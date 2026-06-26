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
    val language: String? = null,
    val subtitles: List<Subtitle> = emptyList(),
    val behaviorHints: BehaviorHints? = null,
    val cached: Boolean = false
)

data class Subtitle(
    val id: String,
    val lang: String,
    val langCode: String = "",
    val url: String,
    val isExternal: Boolean = false,
    val delayMs: Long = 0L
)

data class SubtitleSearchResult(
    val id: String,
    val title: String,
    val lang: String,
    val langCode: String,
    val url: String,
    val downloadCount: Int = 0,
    val rating: Double = 0.0
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
    /** [addonId] identifies which addon produced this ranked snapshot, so callers can remove it from their in-progress set. */
    data class Success(val streams: List<Stream>, val addonId: String) : StreamResult()
    data class Error(val message: String, val addonId: String) : StreamResult()
    data class Loading(val addonId: String) : StreamResult()
}

enum class AddonType {
    TORRENTIO, MEDIAFUSION, KNIGHTCRAWLER, JACKETTIO, COMET, CINEMETA, CUSTOM
}

data class AudioTrack(
    val id: Int,
    val label: String,
    val language: String,
    val isSelected: Boolean = false
)

data class TorrentStatus(
    val infoHash: String,
    val name: String?,
    val progress: Float = 0f,
    val downloadSpeedBps: Long = 0L,
    val uploadSpeedBps: Long = 0L,
    val seeds: Int = 0,
    val peers: Int = 0,
    val totalSize: Long = 0L,
    val downloadedSize: Long = 0L,
    val etaSeconds: Long = 0L,
    val isStreaming: Boolean = false,
    val streamPort: Int = 0,
    val state: TorrentState = TorrentState.CHECKING
)

enum class TorrentState {
    CHECKING, DOWNLOADING, SEEDING, FINISHED, ERROR, STOPPED, PREBUFFERING
}
