package com.streamvault.app.data.repository

import com.streamvault.app.BuildConfig
import com.streamvault.app.data.remote.api.TmdbApi
import com.streamvault.app.domain.model.*
import com.streamvault.app.domain.repository.TmdbRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbRepositoryImpl @Inject constructor(
    private val api: TmdbApi
) : TmdbRepository {

    private val apiKey = BuildConfig.TMDB_API_KEY

    override suspend fun getTrending(page: Int): Result<List<Movie>> = runCatching {
        api.getTrending(apiKey, page).results.map { it.toMovie() }
    }.onFailure { Timber.e(it, "Failed to get trending") }

    override suspend fun getTrendingMovies(page: Int): Result<List<Movie>> = runCatching {
        api.getTrendingMovies(apiKey, page).results.map { it.toMovie() }
    }.onFailure { Timber.e(it, "Failed to get trending movies") }

    override suspend fun getTrendingTv(page: Int): Result<List<TvShow>> = runCatching {
        api.getTrendingTv(apiKey, page).results
            .filter { it.mediaType == "tv" || it.name != null }
            .map {
                TvShow(
                    id = it.id,
                    name = it.name ?: it.title ?: "Unknown",
                    overview = it.overview,
                    posterPath = it.posterPath,
                    backdropPath = it.backdropPath,
                    firstAirDate = it.firstAirDate ?: it.releaseDate ?: "",
                    voteAverage = it.voteAverage,
                    voteCount = it.voteCount,
                    genreIds = it.genreIds ?: emptyList()
                )
            }
    }.onFailure { Timber.e(it, "Failed to get trending TV") }

    override suspend fun getPopularMovies(page: Int): Result<List<Movie>> = runCatching {
        api.getPopularMovies(apiKey, page).results.map { it.toMovie() }
    }.onFailure { Timber.e(it, "Failed to get popular movies") }

    override suspend fun getPopularTv(page: Int): Result<List<TvShow>> = runCatching {
        api.getPopularTv(apiKey, page).results.map { it.toTvShow() }
    }.onFailure { Timber.e(it, "Failed to get popular TV") }

    override suspend fun getTopRatedMovies(page: Int): Result<List<Movie>> = runCatching {
        api.getTopRatedMovies(apiKey, page).results.map { it.toMovie() }
    }.onFailure { Timber.e(it, "Failed to get top rated movies") }

    override suspend fun getNowPlayingMovies(page: Int): Result<List<Movie>> = runCatching {
        api.getNowPlayingMovies(apiKey, page).results.map { it.toMovie() }
    }.onFailure { Timber.e(it, "Failed to get now playing movies") }

    override suspend fun getUpcomingMovies(page: Int): Result<List<Movie>> = runCatching {
        api.getUpcomingMovies(apiKey, page).results.map { it.toMovie() }
    }.onFailure { Timber.e(it, "Failed to get upcoming movies") }

    override suspend fun getMovieDetails(movieId: Int): Result<MovieDetails> = runCatching {
        api.getMovieDetails(movieId, apiKey).toMovieDetails()
    }.onFailure { Timber.e(it, "Failed to get movie details for $movieId") }

    override suspend fun getTvDetails(tvId: Int): Result<TvDetails> = runCatching {
        api.getTvDetails(tvId, apiKey).toTvDetails()
    }.onFailure { Timber.e(it, "Failed to get TV details for $tvId") }

    override suspend fun searchMulti(query: String, page: Int): Result<List<Movie>> = runCatching {
        api.searchMulti(apiKey, query, page).results.map { it.toMovie() }
    }.onFailure { Timber.e(it, "Failed to search for $query") }

    override suspend fun searchMovies(query: String, page: Int): Result<List<Movie>> = runCatching {
        api.searchMovies(apiKey, query, page).results.map { it.toMovie() }
    }.onFailure { Timber.e(it, "Failed to search movies for $query") }

    override suspend fun getMovieGenres(): Result<List<Genre>> = runCatching {
        api.getMovieGenres(apiKey).genres.map { Genre(it.id, it.name) }
    }.onFailure { Timber.e(it, "Failed to get genres") }
}
