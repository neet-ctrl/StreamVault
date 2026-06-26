package com.streamvault.app.domain.repository

import com.streamvault.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface TmdbRepository {
    suspend fun getTrending(page: Int = 1): Result<List<Movie>>
    suspend fun getTrendingMovies(page: Int = 1): Result<List<Movie>>
    suspend fun getTrendingTv(page: Int = 1): Result<List<TvShow>>
    suspend fun getPopularMovies(page: Int = 1): Result<List<Movie>>
    suspend fun getPopularTv(page: Int = 1): Result<List<TvShow>>
    suspend fun getTopRatedMovies(page: Int = 1): Result<List<Movie>>
    suspend fun getNowPlayingMovies(page: Int = 1): Result<List<Movie>>
    suspend fun getUpcomingMovies(page: Int = 1): Result<List<Movie>>
    suspend fun getMovieDetails(movieId: Int): Result<MovieDetails>
    suspend fun getTvDetails(tvId: Int): Result<TvDetails>
    suspend fun searchMulti(query: String, page: Int = 1): Result<List<Movie>>
    suspend fun searchMovies(query: String, page: Int = 1): Result<List<Movie>>
    suspend fun getMovieGenres(): Result<List<Genre>>
}

interface StreamRepository {
    suspend fun getMovieStreams(imdbId: String): Flow<StreamResult>
    suspend fun getTvStreams(imdbId: String, season: Int, episode: Int): Flow<StreamResult>
    fun getActiveAddons(): List<StreamAddon>
    fun getAllAddons(): List<StreamAddon>
    suspend fun toggleAddon(addonId: String, enabled: Boolean)
}

interface LocalRepository {
    // Watch History
    fun getWatchHistory(): Flow<List<WatchHistory>>
    suspend fun addToHistory(item: WatchHistory)
    suspend fun updateProgress(movieId: Int, progressMs: Long, durationMs: Long)
    suspend fun removeFromHistory(movieId: Int)
    suspend fun clearHistory()
    suspend fun getHistoryForMovie(movieId: Int): WatchHistory?

    // Favorites
    fun getFavorites(): Flow<List<Favorite>>
    fun isFavorite(movieId: Int): Flow<Boolean>
    suspend fun addFavorite(favorite: Favorite)
    suspend fun removeFavorite(movieId: Int)

    // Downloads
    fun getDownloads(): Flow<List<Download>>
    suspend fun getDownload(id: String): Download?
    suspend fun insertDownload(download: Download)
    suspend fun updateDownloadProgress(id: String, downloaded: Long, status: DownloadStatus)
    suspend fun deleteDownload(id: String)

    // Playback Positions (continue watching)
    suspend fun savePlaybackPosition(position: PlaybackPosition)
    suspend fun getPlaybackPosition(contentId: String): PlaybackPosition?
    suspend fun getPlaybackPositionByMovieId(movieId: Int): PlaybackPosition?
    fun getContinueWatching(limit: Int = 20): Flow<List<PlaybackPosition>>
    suspend fun deletePlaybackPosition(contentId: String)

    // Subtitle Preferences
    suspend fun getSubtitlePreferences(): SubtitlePreference
    fun getSubtitlePreferencesFlow(): Flow<SubtitlePreference?>
    suspend fun saveSubtitlePreferences(prefs: SubtitlePreference)
    suspend fun updateSubtitleDelay(delayMs: Long)
}
