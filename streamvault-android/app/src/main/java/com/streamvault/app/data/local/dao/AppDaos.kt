package com.streamvault.app.data.local.dao

import androidx.room.*
import com.streamvault.app.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 20): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE movieId = :movieId LIMIT 1")
    suspend fun getHistoryForMovie(movieId: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entity: HistoryEntity)

    @Delete
    suspend fun deleteHistory(entity: HistoryEntity)

    @Query("DELETE FROM watch_history WHERE id = :id")
    suspend fun deleteHistoryById(id: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()

    @Query("SELECT COUNT(*) FROM watch_history")
    suspend fun getHistoryCount(): Int
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE movieId = :movieId LIMIT 1")
    suspend fun getFavorite(movieId: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE movieId = :movieId")
    suspend fun deleteFavorite(movieId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE movieId = :movieId)")
    fun isFavorite(movieId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownload(id: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE status = :status")
    fun getDownloadsByStatus(status: String): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(entity: DownloadEntity)

    @Query("UPDATE downloads SET downloadedSize = :downloaded, status = :status, seedCount = :seeds, peerCount = :peers, downloadSpeedBps = :speed, etaSeconds = :eta WHERE id = :id")
    suspend fun updateDownloadProgress(id: String, downloaded: Long, status: String, seeds: Int = 0, peers: Int = 0, speed: Long = 0L, eta: Long = 0L)

    @Query("UPDATE downloads SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, status: String, completedAt: Long? = null)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownload(id: String)

    @Query("SELECT COUNT(*) FROM downloads WHERE status = 'DOWNLOADING'")
    suspend fun getActiveDownloadCount(): Int
}

@Dao
interface CachedMovieDao {
    @Query("SELECT * FROM cached_movies WHERE id = :id LIMIT 1")
    suspend fun getMovie(id: String): CachedMovieEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<CachedMovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: CachedMovieEntity)

    @Query("DELETE FROM cached_movies WHERE cachedAt < :expiry")
    suspend fun deleteExpired(expiry: Long)

    @Query("DELETE FROM cached_movies")
    suspend fun clearAll()
}

@Dao
interface PlaybackPositionDao {
    @Query("SELECT * FROM playback_positions WHERE contentId = :contentId LIMIT 1")
    suspend fun getPosition(contentId: String): PlaybackPositionEntity?

    @Query("SELECT * FROM playback_positions WHERE movieId = :movieId LIMIT 1")
    suspend fun getPositionByMovieId(movieId: String): PlaybackPositionEntity?

    @Query("SELECT * FROM playback_positions ORDER BY updatedAt DESC")
    fun getAllPositions(): Flow<List<PlaybackPositionEntity>>

    @Query("SELECT * FROM playback_positions WHERE positionMs > 0 AND positionMs < durationMs * 0.9 ORDER BY updatedAt DESC LIMIT :limit")
    fun getContinueWatching(limit: Int = 20): Flow<List<PlaybackPositionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: PlaybackPositionEntity)

    @Query("DELETE FROM playback_positions WHERE contentId = :contentId")
    suspend fun deletePosition(contentId: String)

    @Query("DELETE FROM playback_positions WHERE movieId = :movieId")
    suspend fun deletePositionByMovieId(movieId: String)

    @Query("DELETE FROM playback_positions")
    suspend fun clearAll()
}

@Dao
interface SubtitlePreferenceDao {
    @Query("SELECT * FROM subtitle_preferences WHERE id = 1 LIMIT 1")
    suspend fun getPreferences(): SubtitlePreferenceEntity?

    @Query("SELECT * FROM subtitle_preferences WHERE id = 1 LIMIT 1")
    fun getPreferencesFlow(): Flow<SubtitlePreferenceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreferences(prefs: SubtitlePreferenceEntity)

    @Query("UPDATE subtitle_preferences SET delayMs = :delayMs WHERE id = 1")
    suspend fun updateDelay(delayMs: Long)
}
