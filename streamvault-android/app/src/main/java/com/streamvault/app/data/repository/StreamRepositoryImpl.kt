package com.streamvault.app.data.repository

import com.streamvault.app.data.remote.api.StremioApi
import com.streamvault.app.data.remote.api.StremioUrlBuilder
import com.streamvault.app.domain.model.*
import com.streamvault.app.domain.repository.StreamRepository
import com.streamvault.app.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamRepositoryImpl @Inject constructor(
    private val api: StremioApi
) : StreamRepository {

    private val defaultAddons = listOf(
        StreamAddon(
            id = "torrentio",
            name = "Torrentio",
            baseUrl = Constants.TORRENTIO_URL,
            config = Constants.TORRENTIO_CONFIG,
            priority = 1
        ),
        StreamAddon(
            id = "knightcrawler",
            name = "KnightCrawler",
            baseUrl = Constants.KNIGHTCRAWLER_URL,
            config = Constants.KNIGHTCRAWLER_CONFIG,
            priority = 2
        ),
        StreamAddon(
            id = "mediafusion",
            name = "MediaFusion",
            baseUrl = Constants.MEDIAFUSION_URL,
            config = Constants.MEDIAFUSION_CONFIG,
            priority = 3
        ),
        StreamAddon(
            id = "comet",
            name = "Comet",
            baseUrl = Constants.COMET_URL,
            config = Constants.COMET_CONFIG,
            priority = 4
        ),
        StreamAddon(
            id = "jackettio",
            name = "Jackettio",
            baseUrl = Constants.JACKETTIO_URL,
            config = Constants.JACKETTIO_CONFIG,
            priority = 5
        ),
        StreamAddon(
            id = "cinemeta",
            name = "Cinemeta",
            baseUrl = Constants.CINEMETA_URL,
            config = "",
            priority = 6,
            supportsMovies = true,
            supportsTv = true
        )
    )

    override suspend fun getMovieStreams(imdbId: String): Flow<StreamResult> = flow {
        getActiveAddons().forEach { addon ->
            emit(StreamResult.Loading(addon.id))
            try {
                val url = StremioUrlBuilder.buildMovieStreamUrl(addon.baseUrl, addon.config, imdbId)
                val response = api.getStreams(url)
                val streams = response.streams
                    ?.map { it.toStream(addon.id, addon.name) }
                    ?.sortedByDescending { it.seeds ?: 0 }
                    ?: emptyList()
                emit(StreamResult.Success(streams))
            } catch (e: Exception) {
                Timber.e(e, "Failed to get streams from ${addon.id}")
                emit(StreamResult.Error(e.message ?: "Unknown error", addon.id))
            }
        }
    }

    override suspend fun getTvStreams(imdbId: String, season: Int, episode: Int): Flow<StreamResult> = flow {
        getActiveAddons().filter { it.supportsTv }.forEach { addon ->
            emit(StreamResult.Loading(addon.id))
            try {
                val url = StremioUrlBuilder.buildTvStreamUrl(addon.baseUrl, addon.config, imdbId, season, episode)
                val response = api.getStreams(url)
                val streams = response.streams
                    ?.map { it.toStream(addon.id, addon.name) }
                    ?.sortedByDescending { it.seeds ?: 0 }
                    ?: emptyList()
                emit(StreamResult.Success(streams))
            } catch (e: Exception) {
                Timber.e(e, "Failed to get TV streams from ${addon.id}")
                emit(StreamResult.Error(e.message ?: "Unknown error", addon.id))
            }
        }
    }

    override fun getActiveAddons(): List<StreamAddon> =
        defaultAddons.filter { it.isEnabled }.sortedBy { it.priority }

    override fun getAllAddons(): List<StreamAddon> = defaultAddons

    override suspend fun toggleAddon(addonId: String, enabled: Boolean) {
        // In-memory toggle — for persistence use DataStore
        Timber.d("Toggle addon $addonId: $enabled")
    }
}
