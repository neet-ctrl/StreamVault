package com.streamvault.app.data.remote.api

import com.streamvault.app.data.remote.dto.StremioManifestResponse
import com.streamvault.app.data.remote.dto.StremioMetaResponse
import com.streamvault.app.data.remote.dto.StremioStreamResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface StremioApi {

    @GET
    suspend fun getManifest(@Url url: String): StremioManifestResponse

    @GET
    suspend fun getStreams(@Url url: String): StremioStreamResponse

    @GET
    suspend fun getMeta(@Url url: String): StremioMetaResponse
}

object StremioUrlBuilder {
    fun buildStreamUrl(baseUrl: String, config: String, type: String, id: String): String {
        val configPart = if (config.isNotEmpty()) "/$config" else ""
        return "$baseUrl$configPart/stream/$type/$id.json"
    }

    fun buildMovieStreamUrl(baseUrl: String, config: String, imdbId: String): String =
        buildStreamUrl(baseUrl, config, "movie", imdbId)

    fun buildTvStreamUrl(baseUrl: String, config: String, imdbId: String, season: Int, episode: Int): String =
        buildStreamUrl(baseUrl, config, "series", "$imdbId:$season:$episode")

    fun buildManifestUrl(baseUrl: String): String = "$baseUrl/manifest.json"

    fun buildMetaUrl(baseUrl: String, type: String, id: String): String =
        "$baseUrl/meta/$type/$id.json"
}
