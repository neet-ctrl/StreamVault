package com.streamvault.app.data.repository

import android.net.Uri
import com.streamvault.app.data.remote.api.CinemetaApi
import com.streamvault.app.domain.model.*
import com.streamvault.app.domain.repository.CinemetaRepository
import com.streamvault.app.util.Constants
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CinemetaRepositoryImpl @Inject constructor(
    private val api: CinemetaApi
) : CinemetaRepository {

    override suspend fun getTopMovies(): Result<List<Movie>> = runCatching {
        api.getTopMovies().metas?.map { it.toMovie() } ?: emptyList()
    }.onFailure { Timber.e(it, "Cinemeta: failed to get top movies") }

    override suspend fun getTopSeries(): Result<List<TvShow>> = runCatching {
        api.getTopSeries().metas?.map { it.toTvShow() } ?: emptyList()
    }.onFailure { Timber.e(it, "Cinemeta: failed to get top series") }

    override suspend fun searchMovies(query: String): Result<List<Movie>> = runCatching {
        val encoded = Uri.encode(query)
        val url = "${Constants.CINEMETA_URL}/catalog/movie/top/search=$encoded.json"
        api.search(url).metas?.map { it.toMovie() } ?: emptyList()
    }.onFailure { Timber.e(it, "Cinemeta: failed to search movies for '$query'") }

    override suspend fun searchSeries(query: String): Result<List<TvShow>> = runCatching {
        val encoded = Uri.encode(query)
        val url = "${Constants.CINEMETA_URL}/catalog/series/top/search=$encoded.json"
        api.search(url).metas?.map { it.toTvShow() } ?: emptyList()
    }.onFailure { Timber.e(it, "Cinemeta: failed to search series for '$query'") }

    override suspend fun getMovieMeta(imdbId: String): Result<MovieDetails> = runCatching {
        api.getMovieMeta(imdbId).meta?.toMovieDetails()
            ?: error("Cinemeta returned no meta for movie $imdbId")
    }.onFailure { Timber.e(it, "Cinemeta: failed to get movie meta for $imdbId") }

    override suspend fun getSeriesMeta(imdbId: String): Result<TvDetails> = runCatching {
        api.getSeriesMeta(imdbId).meta?.toTvDetails()
            ?: error("Cinemeta returned no meta for series $imdbId")
    }.onFailure { Timber.e(it, "Cinemeta: failed to get series meta for $imdbId") }
}
