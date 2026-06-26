package com.streamvault.app.engine

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import com.streamvault.app.domain.model.Subtitle
import com.streamvault.app.domain.model.SubtitleSearchResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages subtitle loading, search (OpenSubtitles REST API), caching, and delay.
 */
@Singleton
class SubtitleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient()
    private val cacheDir = File(context.cacheDir, "subtitles").also { it.mkdirs() }

    companion object {
        const val OPENSUBTITLES_API = "https://rest.opensubtitles.org/search"
        val SUPPORTED_LANGUAGES = mapOf(
            "English" to "en",
            "Hindi" to "hi",
            "Tamil" to "ta",
            "Telugu" to "te",
            "Malayalam" to "ml",
            "Kannada" to "kn",
            "Spanish" to "es",
            "French" to "fr",
            "German" to "de",
            "Japanese" to "ja",
            "Korean" to "ko",
            "Chinese" to "zh",
            "Arabic" to "ar",
            "Portuguese" to "pt",
            "Russian" to "ru"
        )
    }

    /**
     * Search for subtitles by IMDB ID and language.
     */
    suspend fun searchByImdbId(imdbId: String, langCode: String = "en"): List<SubtitleSearchResult> =
        withContext(Dispatchers.IO) {
            try {
                val cleanId = imdbId.removePrefix("tt")
                val url = "$OPENSUBTITLES_API/imdbid-$cleanId/sublanguageid-$langCode"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "StreamVault v1.0")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext emptyList()

                val body = response.body?.string() ?: return@withContext emptyList()
                parseOpenSubtitlesResponse(body)
            } catch (e: Exception) {
                Timber.e(e, "SubtitleManager: Search failed for $imdbId")
                emptyList()
            }
        }

    /**
     * Search for subtitles by video hash + file size (best match).
     */
    suspend fun searchByHash(videoHash: String, fileSize: Long, langCode: String = "en"): List<SubtitleSearchResult> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$OPENSUBTITLES_API/moviehash-$videoHash/moviebytesize-$fileSize/sublanguageid-$langCode"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "StreamVault v1.0")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                parseOpenSubtitlesResponse(body)
            } catch (e: Exception) {
                Timber.e(e, "SubtitleManager: Hash search failed")
                emptyList()
            }
        }

    private fun parseOpenSubtitlesResponse(json: String): List<SubtitleSearchResult> {
        return try {
            val array = org.json.JSONArray(json)
            (0 until minOf(array.length(), 20)).mapNotNull { i ->
                try {
                    val obj = array.getJSONObject(i)
                    SubtitleSearchResult(
                        id = obj.optString("IDSubtitleFile"),
                        title = obj.optString("MovieName"),
                        lang = obj.optString("LanguageName"),
                        langCode = obj.optString("ISO639"),
                        url = obj.optString("SubDownloadLink"),
                        downloadCount = obj.optInt("SubDownloadsCnt", 0),
                        rating = obj.optDouble("SubRating", 0.0)
                    )
                } catch (_: Exception) { null }
            }
        } catch (e: Exception) {
            Timber.e(e, "SubtitleManager: Parse failed")
            emptyList()
        }
    }

    /**
     * Download and cache a subtitle file locally.
     * Returns local file path for ExoPlayer injection.
     */
    suspend fun downloadSubtitle(url: String, id: String): String? = withContext(Dispatchers.IO) {
        val cacheFile = File(cacheDir, "$id.srt")
        if (cacheFile.exists()) return@withContext cacheFile.absolutePath
        try {
            val request = Request.Builder().url(url).header("User-Agent", "StreamVault v1.0").get().build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val bytes = response.body?.bytes() ?: return@withContext null
            // Handle gzip-compressed .gz files
            val finalBytes = if (url.endsWith(".gz")) {
                java.util.zip.GZIPInputStream(bytes.inputStream()).readBytes()
            } else bytes
            cacheFile.writeBytes(finalBytes)
            cacheFile.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "SubtitleManager: Download failed for $url")
            null
        }
    }

    /**
     * Build a Media3 subtitle configuration for ExoPlayer.
     */
    fun buildSubtitleConfig(filePath: String, langCode: String): MediaItem.SubtitleConfiguration {
        val mimeType = when {
            filePath.endsWith(".srt") -> MimeTypes.APPLICATION_SUBRIP
            filePath.endsWith(".vtt") -> MimeTypes.TEXT_VTT
            filePath.endsWith(".ass") || filePath.endsWith(".ssa") -> MimeTypes.TEXT_SSA
            else -> MimeTypes.APPLICATION_SUBRIP
        }
        return MediaItem.SubtitleConfiguration.Builder(
            android.net.Uri.fromFile(File(filePath))
        )
            .setMimeType(mimeType)
            .setLanguage(langCode)
            .setSelectionFlags(androidx.media3.common.C.SELECTION_FLAG_DEFAULT)
            .build()
    }

    /**
     * Apply delay offset to SRT subtitle file. Creates a new cached file.
     */
    suspend fun applyDelay(filePath: String, delayMs: Long): String = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            val content = file.readText()
            val timeRegex = Regex("""(\d{2}):(\d{2}):(\d{2}),(\d{3})""")
            val shifted = content.replace(timeRegex) { match ->
                val h = match.groupValues[1].toLong()
                val m = match.groupValues[2].toLong()
                val s = match.groupValues[3].toLong()
                val ms = match.groupValues[4].toLong()
                val totalMs = (h * 3600_000 + m * 60_000 + s * 1000 + ms + delayMs).coerceAtLeast(0)
                val nh = totalMs / 3600_000
                val nm = (totalMs % 3600_000) / 60_000
                val ns = (totalMs % 60_000) / 1000
                val nms = totalMs % 1000
                "%02d:%02d:%02d,%03d".format(nh, nm, ns, nms)
            }
            val delayedFile = File(cacheDir, "${file.nameWithoutExtension}_delayed_${delayMs}.srt")
            delayedFile.writeText(shifted)
            delayedFile.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "SubtitleManager: Delay apply failed")
            filePath
        }
    }

    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}
