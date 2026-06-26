package com.streamvault.app.domain.model

data class Stream(
    val id: String,
    val title: String,
    val url: String,
    val infoHash: String? = null,
    val fileIdx: Int? = null,
    val quality: String,
    val size: Long? = null,
    val seeds: Int? = null,
    val peers: Int? = null,
    val provider: String,
    val addonId: String,
    val isTorrent: Boolean = false,
    val subtitles: List<Subtitle> = emptyList(),
    val behaviorHints: BehaviorHints? = null
)

data class Subtitle(
    val id: String,
    val lang: String,
    val url: String
)

data class BehaviorHints(
    val countryWhitelist: List<String>? = null,
    val notWebReady: Boolean = false,
    val videoHash: String? = null,
    val videoSize: Long? = null
)

data class StreamAddon(
    val id: String,
    val name: String,
    val baseUrl: String,
    val config: String = "",
    val isEnabled: Boolean = true,
    val priority: Int = 0,
    val supportsMovies: Boolean = true,
    val supportsTv: Boolean = true
)

sealed class StreamResult {
    data class Success(val streams: List<Stream>) : StreamResult()
    data class Error(val message: String, val addonId: String) : StreamResult()
    data class Loading(val addonId: String) : StreamResult()
}

enum class AddonType {
    TORRENTIO, MEDIAFUSION, KNIGHTCRAWLER, JACKETTIO, COMET, CINEMETA, CUSTOM
}
