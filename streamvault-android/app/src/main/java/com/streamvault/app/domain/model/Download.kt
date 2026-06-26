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
    val fileIdx: Int? = null
)

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
    val activeAddons: List<String> = listOf("torrentio", "knightcrawler", "mediafusion", "comet")
)
