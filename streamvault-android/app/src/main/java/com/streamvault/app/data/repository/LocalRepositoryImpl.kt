package com.streamvault.app.data.repository

import com.streamvault.app.data.local.dao.*
import com.streamvault.app.data.local.entities.*
import com.streamvault.app.domain.model.*
import com.streamvault.app.domain.repository.LocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao,
    private val favoriteDao: FavoriteDao,
    private val downloadDao: DownloadDao
) : LocalRepository {

    override fun getWatchHistory(): Flow<List<WatchHistory>> =
        historyDao.getAllHistory().map { list ->
            list.map { entity ->
                WatchHistory(
                    id = entity.id,
                    movieId = entity.movieId,
                    title = entity.title,
                    posterPath = entity.posterPath,
                    mediaType = entity.mediaType,
                    watchedAt = entity.watchedAt,
                    progressMs = entity.progressMs,
                    durationMs = entity.durationMs,
                    season = entity.season,
                    episode = entity.episode
                )
            }
        }

    override suspend fun addToHistory(item: WatchHistory) {
        historyDao.insertHistory(
            HistoryEntity(
                id = item.id,
                movieId = item.movieId,
                title = item.title,
                posterPath = item.posterPath,
                mediaType = item.mediaType,
                watchedAt = item.watchedAt,
                progressMs = item.progressMs,
                durationMs = item.durationMs,
                season = item.season,
                episode = item.episode
            )
        )
    }

    override suspend fun updateProgress(movieId: Int, progressMs: Long, durationMs: Long) {
        val existing = historyDao.getHistoryForMovie(movieId)
        if (existing != null) {
            historyDao.insertHistory(existing.copy(progressMs = progressMs, durationMs = durationMs, watchedAt = System.currentTimeMillis()))
        }
    }

    override suspend fun removeFromHistory(movieId: Int) {
        val existing = historyDao.getHistoryForMovie(movieId)
        existing?.let { historyDao.deleteHistory(it) }
    }

    override suspend fun clearHistory() = historyDao.clearAllHistory()

    override suspend fun getHistoryForMovie(movieId: Int): WatchHistory? =
        historyDao.getHistoryForMovie(movieId)?.let { entity ->
            WatchHistory(
                id = entity.id,
                movieId = entity.movieId,
                title = entity.title,
                posterPath = entity.posterPath,
                mediaType = entity.mediaType,
                watchedAt = entity.watchedAt,
                progressMs = entity.progressMs,
                durationMs = entity.durationMs
            )
        }

    override fun getFavorites(): Flow<List<Favorite>> =
        favoriteDao.getAllFavorites().map { list ->
            list.map { entity ->
                Favorite(
                    id = entity.id,
                    movieId = entity.movieId,
                    title = entity.title,
                    posterPath = entity.posterPath,
                    mediaType = entity.mediaType,
                    addedAt = entity.addedAt,
                    rating = entity.rating
                )
            }
        }

    override fun isFavorite(movieId: Int): Flow<Boolean> = favoriteDao.isFavorite(movieId)

    override suspend fun addFavorite(favorite: Favorite) {
        favoriteDao.insertFavorite(
            FavoriteEntity(
                id = favorite.id,
                movieId = favorite.movieId,
                title = favorite.title,
                posterPath = favorite.posterPath,
                mediaType = favorite.mediaType,
                addedAt = favorite.addedAt,
                rating = favorite.rating
            )
        )
    }

    override suspend fun removeFavorite(movieId: Int) = favoriteDao.deleteFavorite(movieId)

    override fun getDownloads(): Flow<List<Download>> =
        downloadDao.getAllDownloads().map { list ->
            list.map { entity ->
                Download(
                    id = entity.id,
                    movieId = entity.movieId,
                    title = entity.title,
                    posterPath = entity.posterPath,
                    quality = entity.quality,
                    filePath = entity.filePath,
                    totalSize = entity.totalSize,
                    downloadedSize = entity.downloadedSize,
                    status = DownloadStatus.valueOf(entity.status),
                    createdAt = entity.createdAt,
                    completedAt = entity.completedAt,
                    infoHash = entity.infoHash,
                    fileIdx = entity.fileIdx
                )
            }
        }

    override suspend fun getDownload(id: String): Download? =
        downloadDao.getDownload(id)?.let { entity ->
            Download(
                id = entity.id,
                movieId = entity.movieId,
                title = entity.title,
                posterPath = entity.posterPath,
                quality = entity.quality,
                filePath = entity.filePath,
                totalSize = entity.totalSize,
                downloadedSize = entity.downloadedSize,
                status = DownloadStatus.valueOf(entity.status)
            )
        }

    override suspend fun insertDownload(download: Download) {
        downloadDao.insertDownload(
            DownloadEntity(
                id = download.id,
                movieId = download.movieId,
                title = download.title,
                posterPath = download.posterPath,
                quality = download.quality,
                filePath = download.filePath,
                totalSize = download.totalSize,
                downloadedSize = download.downloadedSize,
                status = download.status.name,
                createdAt = download.createdAt,
                completedAt = download.completedAt,
                infoHash = download.infoHash,
                fileIdx = download.fileIdx
            )
        )
    }

    override suspend fun updateDownloadProgress(id: String, downloaded: Long, status: DownloadStatus) {
        downloadDao.updateDownloadProgress(id, downloaded, status.name)
    }

    override suspend fun deleteDownload(id: String) = downloadDao.deleteDownload(id)
}
