package com.streamvault.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class HistoryEntity(
    @PrimaryKey val id: String,
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val mediaType: String,
    val watchedAt: Long = System.currentTimeMillis(),
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val season: Int? = null,
    val episode: Int? = null
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val mediaType: String,
    val addedAt: Long = System.currentTimeMillis(),
    val rating: Double = 0.0
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val quality: String,
    val filePath: String,
    val totalSize: Long,
    val downloadedSize: Long,
    val status: String,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val infoHash: String? = null,
    val fileIdx: Int? = null,
    val seedCount: Int = 0,
    val peerCount: Int = 0,
    val downloadSpeedBps: Long = 0L,
    val etaSeconds: Long = 0L
)

@Entity(tableName = "cached_movies")
data class CachedMovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val genreIds: String,
    val mediaType: String,
    val imdbId: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playback_positions")
data class PlaybackPositionEntity(
    @PrimaryKey val contentId: String,
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

    val isFinished: Boolean
        get() = progressPercent >= 90.0

    val isStarted: Boolean
        get() = progressPercent >= 1.0
}

@Entity(tableName = "subtitle_preferences")
data class SubtitlePreferenceEntity(
    @PrimaryKey val id: Int = 1,
    val language: String = "English",
    val languageCode: String = "en",
    val sizeScale: Float = 1.0f,
    val fontFamily: String = "Default",
    val textColor: String = "#FFFFFF",
    val backgroundColor: String = "#80000000",
    val boldEnabled: Boolean = false,
    val italicEnabled: Boolean = false,
    val outlineEnabled: Boolean = true,
    val delayMs: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)
