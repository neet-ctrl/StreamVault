package com.streamvault.app.domain.repository

import com.streamvault.app.domain.model.*
import kotlinx.coroutines.flow.Flow

// ─── Cinemeta (replaces TMDB — no API key required) ──────────────────────────

interface CinemetaRepository {
    suspend fun getTopMovies(): Result<List<Movie>>
    suspend fun getTopSeries(): Result<List<TvShow>>
    suspend fun searchMovies(query: String): Result<List<Movie>>
    suspend fun searchSeries(query: String): Result<List<TvShow>>
    suspend fun getMovieMeta(imdbId: String): Result<MovieDetails>
    suspend fun getSeriesMeta(imdbId: String): Result<TvDetails>
}

// ─── Stream (Torrentio + other addons) ───────────────────────────────────────

interface StreamRepository {
    suspend fun getMovieStreams(imdbId: String): Flow<StreamResult>
    suspend fun getTvStreams(imdbId: String, season: Int, episode: Int): Flow<StreamResult>
    fun getActiveAddons(): List<StreamAddon>
    fun getAllAddons(): List<StreamAddon>
    suspend fun toggleAddon(addonId: String, enabled: Boolean)
}

// ─── Local (Room) ─────────────────────────────────────────────────────────────

interface LocalRepository {

    // Watch History
    fun getWatchHistory(): Flow<List<WatchHistory>>
    suspend fun addToHistory(item: WatchHistory)
    suspend fun updateProgress(movieId: String, progressMs: Long, durationMs: Long)
    suspend fun removeFromHistory(movieId: String)
    suspend fun clearHistory()
    suspend fun getHistoryForMovie(movieId: String): WatchHistory?

    // Favorites
    fun getFavorites(): Flow<List<Favorite>>
    fun isFavorite(movieId: String): Flow<Boolean>
    suspend fun addFavorite(favorite: Favorite)
    suspend fun removeFavorite(movieId: String)

    // Downloads
    fun getDownloads(): Flow<List<Download>>
    suspend fun getDownload(id: String): Download?
    suspend fun insertDownload(download: Download)
    suspend fun updateDownloadProgress(id: String, downloaded: Long, status: DownloadStatus)
    suspend fun deleteDownload(id: String)

    // Playback Positions (continue watching)
    suspend fun savePlaybackPosition(position: PlaybackPosition)
    suspend fun getPlaybackPosition(contentId: String): PlaybackPosition?
    suspend fun getPlaybackPositionByMovieId(movieId: String): PlaybackPosition?
    fun getContinueWatching(limit: Int = 20): Flow<List<PlaybackPosition>>
    suspend fun deletePlaybackPosition(contentId: String)

    // Subtitle Preferences
    suspend fun getSubtitlePreferences(): SubtitlePreference
    fun getSubtitlePreferencesFlow(): Flow<SubtitlePreference?>
    suspend fun saveSubtitlePreferences(prefs: SubtitlePreference)
    suspend fun updateSubtitleDelay(delayMs: Long)
}
