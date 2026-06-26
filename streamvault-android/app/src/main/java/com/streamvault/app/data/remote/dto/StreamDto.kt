package com.streamvault.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.streamvault.app.domain.model.Stream
import com.streamvault.app.domain.model.Subtitle
import com.streamvault.app.domain.model.BehaviorHints

data class StremioManifestResponse(
    @SerializedName("id") val id: String,
    @SerializedName("version") val version: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("resources") val resources: List<Any>?,
    @SerializedName("types") val types: List<String>?,
    @SerializedName("catalogs") val catalogs: List<Any>?
)

data class StremioStreamResponse(
    @SerializedName("streams") val streams: List<StremioStreamDto>?
)

data class StremioStreamDto(
    @SerializedName("name") val name: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("infoHash") val infoHash: String?,
    @SerializedName("fileIdx") val fileIdx: Int?,
    @SerializedName("subtitles") val subtitles: List<StremioSubtitleDto>?,
    @SerializedName("behaviorHints") val behaviorHints: StreamBehaviorHintsDto?
) {
    fun toStream(addonId: String, addonName: String): Stream {
        val titleText = title ?: name ?: "Unknown"
        val quality = extractQuality(titleText)
        val size = behaviorHints?.videoSize ?: extractSizeFromTitle(titleText)
        val seeds = extractSeedsFromTitle(titleText)
        val peers = extractPeersFromTitle(titleText)
        val language = extractLanguageFromTitle(titleText)
        val isTorrent = infoHash != null || (url?.startsWith("magnet:") == true)
        val cached = titleText.contains("⚡", ignoreCase = false)
                || titleText.contains("[cache]", ignoreCase = true)

        return Stream(
            id = "${addonId}_${infoHash?.uppercase() ?: url}_${fileIdx}",
            title = titleText,
            url = url ?: (if (infoHash != null) "magnet:?xt=urn:btih:$infoHash" else ""),
            infoHash = infoHash?.uppercase(),
            fileIdx = fileIdx,
            quality = quality,
            size = size,
            seeds = seeds,
            peers = peers,
            provider = addonName,
            addonId = addonId,
            isTorrent = isTorrent,
            language = language,
            subtitles = subtitles?.map { it.toSubtitle() } ?: emptyList(),
            behaviorHints = behaviorHints?.toBehaviorHints(),
            cached = cached
        )
    }

    private fun extractQuality(title: String): String = when {
        title.contains("4K", ignoreCase = true) || title.contains("2160p", ignoreCase = true) -> "4K"
        title.contains("1080p", ignoreCase = true) -> "1080p"
        title.contains("720p", ignoreCase = true) -> "720p"
        title.contains("480p", ignoreCase = true) -> "480p"
        title.contains("360p", ignoreCase = true) -> "360p"
        title.contains("HDR", ignoreCase = true) -> "1080p"
        title.contains("HD", ignoreCase = true) -> "HD"
        else -> "HD"
    }

    private fun extractSizeFromTitle(title: String): Long? {
        val gbRegex = Regex("([0-9]+(?:[.,][0-9]+)?)\\s*GB", RegexOption.IGNORE_CASE)
        val mbRegex = Regex("([0-9]+(?:[.,][0-9]+)?)\\s*MB", RegexOption.IGNORE_CASE)
        val gbMatch = gbRegex.find(title)
        val mbMatch = mbRegex.find(title)
        return when {
            gbMatch != null -> {
                val v = gbMatch.groupValues[1].replace(',', '.').toDoubleOrNull() ?: 0.0
                (v * 1024 * 1024 * 1024).toLong()
            }
            mbMatch != null -> {
                val v = mbMatch.groupValues[1].replace(',', '.').toDoubleOrNull() ?: 0.0
                (v * 1024 * 1024).toLong()
            }
            else -> null
        }
    }

    private fun extractSeedsFromTitle(title: String): Int? {
        val seedRegex = Regex("👤\\s*([0-9,]+)")
        val altRegex = Regex("\\bS:?\\s*([0-9]+)", RegexOption.IGNORE_CASE)
        return seedRegex.find(title)?.groupValues?.get(1)?.replace(",", "")?.toIntOrNull()
            ?: altRegex.find(title)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun extractPeersFromTitle(title: String): Int? {
        val peerRegex = Regex("🔗\\s*([0-9,]+)")
        val altRegex = Regex("\\bP:?\\s*([0-9]+)", RegexOption.IGNORE_CASE)
        return peerRegex.find(title)?.groupValues?.get(1)?.replace(",", "")?.toIntOrNull()
            ?: altRegex.find(title)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun extractLanguageFromTitle(title: String): String? {
        val t = title.uppercase()
        return when {
            t.contains("HINDI") || t.contains("हिंदी") -> "Hindi"
            t.contains("TAMIL") -> "Tamil"
            t.contains("TELUGU") -> "Telugu"
            t.contains("MALAYALAM") -> "Malayalam"
            t.contains("KANNADA") -> "Kannada"
            t.contains("JAPANESE") -> "Japanese"
            t.contains("KOREAN") -> "Korean"
            t.contains("CHINESE") || t.contains("MANDARIN") -> "Chinese"
            t.contains("SPANISH") -> "Spanish"
            t.contains("FRENCH") -> "French"
            t.contains("GERMAN") -> "German"
            t.contains("MULTI") || t.contains("DUAL") -> "Multi"
            else -> null
        }
    }
}

data class StremioSubtitleDto(
    @SerializedName("id") val id: String?,
    @SerializedName("lang") val lang: String,
    @SerializedName("url") val url: String
) {
    fun toSubtitle() = Subtitle(id = id ?: lang, lang = lang, url = url)
}

data class StreamBehaviorHintsDto(
    @SerializedName("countryWhitelist") val countryWhitelist: List<String>?,
    @SerializedName("notWebReady") val notWebReady: Boolean?,
    @SerializedName("videoHash") val videoHash: String?,
    @SerializedName("videoSize") val videoSize: Long?
) {
    fun toBehaviorHints() = BehaviorHints(
        countryWhitelist = countryWhitelist,
        notWebReady = notWebReady ?: false,
        videoHash = videoHash,
        videoSize = videoSize
    )
}

data class StremioMetaResponse(
    @SerializedName("meta") val meta: StremioMetaDto?
)

data class StremioMetaDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("name") val name: String,
    @SerializedName("poster") val poster: String?,
    @SerializedName("background") val background: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("imdbRating") val imdbRating: String?,
    @SerializedName("year") val year: String?,
    @SerializedName("genres") val genres: List<String>?,
    @SerializedName("runtime") val runtime: String?,
    @SerializedName("videos") val videos: List<StremioVideoDto>?
)

data class StremioVideoDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String?,
    @SerializedName("season") val season: Int?,
    @SerializedName("episode") val episode: Int?,
    @SerializedName("released") val released: String?,
    @SerializedName("thumbnail") val thumbnail: String?
)
