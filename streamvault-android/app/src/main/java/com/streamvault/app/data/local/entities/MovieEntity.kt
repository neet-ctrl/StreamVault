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
    val fileIdx: Int? = null
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
    val cachedAt: Long = System.currentTimeMillis()
)
