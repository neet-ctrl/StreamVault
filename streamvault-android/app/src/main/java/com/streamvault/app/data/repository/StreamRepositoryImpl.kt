package com.streamvault.app.data.repository

import com.streamvault.app.data.remote.api.StremioApi
import com.streamvault.app.data.remote.api.StremioUrlBuilder
import com.streamvault.app.domain.model.*
import com.streamvault.app.domain.repository.StreamRepository
import com.streamvault.app.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamRepositoryImpl @Inject constructor(
    private val api: StremioApi
) : StreamRepository {

    private val addonEnabled = mutableMapOf<String, Boolean>()

    private val defaultAddons = listOf(
        StreamAddon(id = "torrentio",     name = "Torrentio",     baseUrl = Constants.TORRENTIO_URL,     config = Constants.TORRENTIO_CONFIG,     priority = 1),
        StreamAddon(id = "knightcrawler", name = "KnightCrawler", baseUrl = Constants.KNIGHTCRAWLER_URL, config = Constants.KNIGHTCRAWLER_CONFIG, priority = 2),
        StreamAddon(id = "mediafusion",   name = "MediaFusion",   baseUrl = Constants.MEDIAFUSION_URL,   config = Constants.MEDIAFUSION_CONFIG,   priority = 3),
        StreamAddon(id = "comet",         name = "Comet",         baseUrl = Constants.COMET_URL,         config = Constants.COMET_CONFIG,         priority = 4),
        StreamAddon(id = "jackettio",     name = "Jackettio",     baseUrl = Constants.JACKETTIO_URL,     config = Constants.JACKETTIO_CONFIG,     priority = 5),
        StreamAddon(id = "cinemeta",      name = "Cinemeta",      baseUrl = Constants.CINEMETA_URL,      config = "",                             priority = 6)
    )

    override suspend fun getMovieStreams(imdbId: String): Flow<StreamResult> = flow {
        val accumulated = mutableListOf<Stream>()
        val seenHashes = mutableSetOf<String>()

        getActiveAddons().forEach { addon ->
            emit(StreamResult.Loading(addon.id))
            var retries = 0
            while (retries < Constants.STREAM_RETRY_COUNT) {
                try {
                    val url = StremioUrlBuilder.buildMovieStreamUrl(addon.baseUrl, addon.config, imdbId)
                    val response = api.getStreams(url)
                    val streams = response.streams
                        ?.map { it.toStream(addon.id, addon.name) }
                        ?.filter { stream ->
                            // Dedup by infoHash or URL
                            val key = stream.infoHash?.uppercase() ?: stream.url
                            seenHashes.add(key)
                        }
                        ?: emptyList()

                    accumulated.addAll(streams)
                    val ranked = StreamRanker.rank(accumulated)
                    emit(StreamResult.Success(ranked, addon.id))
                    break
                } catch (e: Exception) {
                    retries++
                    if (retries >= Constants.STREAM_RETRY_COUNT) {
                        Timber.e(e, "Failed to get streams from ${addon.id} after $retries retries")
                        emit(StreamResult.Error(e.message ?: "Unknown error", addon.id))
                    } else {
                        delay(Constants.STREAM_RETRY_DELAY_MS * retries)
                    }
                }
            }
        }
    }

    override suspend fun getTvStreams(imdbId: String, season: Int, episode: Int): Flow<StreamResult> = flow {
        val accumulated = mutableListOf<Stream>()
        val seenHashes = mutableSetOf<String>()

        getActiveAddons().filter { it.supportsTv }.forEach { addon ->
            emit(StreamResult.Loading(addon.id))
            var retries = 0
            while (retries < Constants.STREAM_RETRY_COUNT) {
                try {
                    val url = StremioUrlBuilder.buildTvStreamUrl(addon.baseUrl, addon.config, imdbId, season, episode)
                    val response = api.getStreams(url)
                    val streams = response.streams
                        ?.map { it.toStream(addon.id, addon.name) }
                        ?.filter { stream ->
                            val key = stream.infoHash?.uppercase() ?: stream.url
                            seenHashes.add(key)
                        }
                        ?: emptyList()

                    accumulated.addAll(streams)
                    val ranked = StreamRanker.rank(accumulated)
                    emit(StreamResult.Success(ranked, addon.id))
                    break
                } catch (e: Exception) {
                    retries++
                    if (retries >= Constants.STREAM_RETRY_COUNT) {
                        Timber.e(e, "Failed to get TV streams from ${addon.id}")
                        emit(StreamResult.Error(e.message ?: "Unknown error", addon.id))
                    } else {
                        delay(Constants.STREAM_RETRY_DELAY_MS * retries)
                    }
                }
            }
        }
    }

    override fun getActiveAddons(): List<StreamAddon> =
        defaultAddons
            .filter { addon -> addonEnabled.getOrDefault(addon.id, addon.isEnabled) }
            .sortedBy { it.priority }

    override fun getAllAddons(): List<StreamAddon> = defaultAddons

    override suspend fun toggleAddon(addonId: String, enabled: Boolean) {
        addonEnabled[addonId] = enabled
        Timber.d("Toggled addon $addonId: $enabled")
    }
}
