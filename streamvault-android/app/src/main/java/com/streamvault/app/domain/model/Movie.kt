package com.streamvault.app.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val genreIds: List<Int>,
    val genres: List<String> = emptyList(),
    val runtime: Int? = null,
    val tagline: String? = null,
    val status: String? = null,
    val imdbId: String? = null,
    val mediaType: MediaType = MediaType.MOVIE,
    val popularity: Double = 0.0,
    val adult: Boolean = false
)

data class TvShow(
    val id: Int,
    val name: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val firstAirDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val genreIds: List<Int>,
    val genres: List<String> = emptyList(),
    val numberOfSeasons: Int = 0,
    val numberOfEpisodes: Int = 0,
    val imdbId: String? = null,
    val mediaType: MediaType = MediaType.TV
)

data class MovieDetails(
    val movie: Movie,
    val cast: List<CastMember>,
    val videos: List<Video>,
    val similar: List<Movie>,
    val recommendations: List<Movie>
)

data class TvDetails(
    val tvShow: TvShow,
    val cast: List<CastMember>,
    val videos: List<Video>,
    val seasons: List<Season>,
    val similar: List<TvShow>
)

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profilePath: String?,
    val order: Int
)

data class Video(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String
)

data class Season(
    val id: Int,
    val seasonNumber: Int,
    val episodeCount: Int,
    val name: String,
    val overview: String,
    val posterPath: String?,
    val airDate: String?
)

data class Episode(
    val id: Int,
    val episodeNumber: Int,
    val name: String,
    val overview: String,
    val stillPath: String?,
    val airDate: String?,
    val runtime: Int?,
    val voteAverage: Double
)

enum class MediaType {
    MOVIE, TV
}

data class Genre(
    val id: Int,
    val name: String
)

data class SearchResult(
    val movies: List<Movie>,
    val tvShows: List<TvShow>,
    val totalResults: Int,
    val totalPages: Int,
    val currentPage: Int
)
