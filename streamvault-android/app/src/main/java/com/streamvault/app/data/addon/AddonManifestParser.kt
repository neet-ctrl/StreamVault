package com.streamvault.app.data.addon

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.streamvault.app.domain.model.StreamAddon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.concurrent.TimeUnit

data class AddonManifest(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("version") val version: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("resources") val resources: List<Any>?,
    @SerializedName("types") val types: List<String>?,
    @SerializedName("catalogs") val catalogs: List<CatalogEntry>?,
    @SerializedName("behaviorHints") val behaviorHints: ManifestBehaviorHints?
)

data class CatalogEntry(
    @SerializedName("type") val type: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?
)

data class ManifestBehaviorHints(
    @SerializedName("adult") val adult: Boolean?,
    @SerializedName("p2p") val p2p: Boolean?,
    @SerializedName("configurable") val configurable: Boolean?,
    @SerializedName("configurationRequired") val configurationRequired: Boolean?
)

class AddonManifestParser {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    suspend fun fetchManifest(baseUrl: String): AddonManifest? = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/manifest.json"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext null
                gson.fromJson(body, AddonManifest::class.java)
            } else {
                Timber.w("Manifest fetch failed for $url: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch manifest from $baseUrl")
            null
        }
    }

    fun manifestToAddon(manifest: AddonManifest, baseUrl: String, config: String = "", priority: Int = 99): StreamAddon {
        val resourceStrings = manifest.resources?.mapNotNull { res ->
            when (res) {
                is String -> res
                is Map<*, *> -> res["name"] as? String
                else -> res.toString()
            }
        } ?: emptyList()

        val supportsStream = resourceStrings.any { it == "stream" }
        val supportsMovies = manifest.types?.contains("movie") == true
        val supportsTv = manifest.types?.contains("series") == true

        return StreamAddon(
            id = manifest.id,
            name = manifest.name,
            baseUrl = baseUrl,
            config = config,
            isEnabled = true,
            priority = priority,
            supportsMovies = supportsMovies || !manifest.types.isNullOrEmpty(),
            supportsTv = supportsTv || !manifest.types.isNullOrEmpty()
        )
    }

    suspend fun fetchAndParseAddon(baseUrl: String, config: String = "", priority: Int = 99): StreamAddon? {
        val manifest = fetchManifest(baseUrl) ?: return null
        return manifestToAddon(manifest, baseUrl, config, priority)
    }

    fun parseResourceSupport(resources: List<Any>?): Set<String> {
        return resources?.mapNotNull { res ->
            when (res) {
                is String -> res
                is Map<*, *> -> res["name"] as? String
                else -> null
            }
        }?.toSet() ?: emptySet()
    }
}
