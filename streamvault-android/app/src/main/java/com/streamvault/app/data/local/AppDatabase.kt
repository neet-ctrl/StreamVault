package com.streamvault.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.streamvault.app.data.local.dao.*
import com.streamvault.app.data.local.entities.*

@Database(
    entities = [
        HistoryEntity::class,
        FavoriteEntity::class,
        DownloadEntity::class,
        CachedMovieEntity::class,
        PlaybackPositionEntity::class,
        SubtitlePreferenceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun downloadDao(): DownloadDao
    abstract fun cachedMovieDao(): CachedMovieDao
    abstract fun playbackPositionDao(): PlaybackPositionDao
    abstract fun subtitlePreferenceDao(): SubtitlePreferenceDao
}
