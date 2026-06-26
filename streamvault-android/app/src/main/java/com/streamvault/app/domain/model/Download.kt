package com.streamvault.app.domain.model

data class Download(
    val id: String,
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val quality: String,
    val filePath: String,
    val totalSize: Long,
    val downloadedSize: Long,
    val status: DownloadStatus,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val infoHash: String? = null,
    val fileIdx: Int? = null,
    val seedCount: Int = 0,
    val peerCount: Int = 0,
    val downloadSpeedBps: Long = 0L,
    val etaSeconds: Long = 0L
) {
    val progressFraction: Float
        get() = if (totalSize > 0) (downloadedSize.toFloat() / totalSize.toFloat()).coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()
}

enum class DownloadStatus {
    QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED
}

data class WatchHistory(
    val id: String,
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val mediaType: String,
    val watchedAt: Long = System.currentTimeMillis(),
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val season: Int? = null,
    val episode: Int? = null
) {
    val progressPercent: Float
        get() = if (durationMs > 0) (progressMs.toFloat() / durationMs.toFloat()) else 0f
}

data class PlaybackPosition(
    val contentId: String,
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val mediaType: String,
    val positionMs: Long,
    val durationMs: Long,
    val season: Int? = null,
    val episode: Int? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val progressPercent: Double
        get() = if (durationMs > 0) (positionMs.toDouble() / durationMs) * 100 else 0.0

    val isFinished: Boolean get() = progressPercent >= 90.0
    val isStarted: Boolean get() = progressPercent >= 1.0
}

data class SubtitlePreference(
    val language: String = "English",
    val languageCode: String = "en",
    val sizeScale: Float = 1.0f,
    val fontFamily: String = "Default",
    val textColor: String = "#FFFFFF",
    val backgroundColor: String = "#80000000",
    val boldEnabled: Boolean = false,
    val italicEnabled: Boolean = false,
    val outlineEnabled: Boolean = true,
    val delayMs: Long = 0L
)

data class Favorite(
    val id: String,
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val mediaType: String,
    val addedAt: Long = System.currentTimeMillis(),
    val rating: Double = 0.0
)

data class UserSettings(
    val preferredQuality: String = "1080p",
    val preferredAudioLang: String = "en",
    val preferredSubLang: String = "en",
    val autoPlayNext: Boolean = true,
    val subtitleSize: Float = 1.0f,
    val downloadPath: String = "Movies/StreamVault",
    val activeAddons: List<String> = listOf("torrentio", "knightcrawler", "mediafusion", "comet"),
    val preBufferSizeMb: Int = 10,
    val maxConnections: Int = 200,
    val torrentTimeoutSeconds: Int = 30,
    val useExternalPlayer: Boolean = false,
    val hardwareAcceleration: Boolean = true,
    val skipIntroEnabled: Boolean = false,
    val autoSkipCredits: Boolean = false
)
