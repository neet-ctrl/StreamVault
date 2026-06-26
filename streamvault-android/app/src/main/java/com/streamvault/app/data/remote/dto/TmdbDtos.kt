package com.streamvault.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.streamvault.app.domain.model.*

data class TmdbMovieListResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val results: List<TmdbMovieDto>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
)

data class TmdbMovieDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("overview") val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("vote_count") val voteCount: Int,
    @SerializedName("genre_ids") val genreIds: List<Int>?,
    @SerializedName("popularity") val popularity: Double,
    @SerializedName("adult") val adult: Boolean?,
    @SerializedName("media_type") val mediaType: String?
) {
    fun toMovie() = Movie(
        id = id,
        title = title ?: name ?: "Unknown",
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate ?: firstAirDate ?: "",
        voteAverage = voteAverage,
        voteCount = voteCount,
        genreIds = genreIds ?: emptyList(),
        popularity = popularity,
        adult = adult ?: false,
        mediaType = if (mediaType == "tv") MediaType.TV else MediaType.MOVIE
    )
}

data class TmdbMovieDetailResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("overview") val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("vote_count") val voteCount: Int,
    @SerializedName("genres") val genres: List<TmdbGenreDto>,
    @SerializedName("runtime") val runtime: Int?,
    @SerializedName("tagline") val tagline: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("popularity") val popularity: Double,
    @SerializedName("external_ids") val externalIds: TmdbExternalIds?,
    @SerializedName("credits") val credits: TmdbCreditsDto?,
    @SerializedName("videos") val videos: TmdbVideosDto?,
    @SerializedName("similar") val similar: TmdbMovieListResponse?,
    @SerializedName("recommendations") val recommendations: TmdbMovieListResponse?
) {
    fun toMovieDetails(): MovieDetails {
        val movie = Movie(
            id = id,
            title = title,
            overview = overview,
            posterPath = posterPath,
            backdropPath = backdropPath,
            releaseDate = releaseDate,
            voteAverage = voteAverage,
            voteCount = voteCount,
            genreIds = genres.map { it.id },
            genres = genres.map { it.name },
            runtime = runtime,
            tagline = tagline,
            status = status,
            popularity = popularity,
            imdbId = externalIds?.imdbId
        )
        return MovieDetails(
            movie = movie,
            cast = credits?.cast?.take(20)?.map { it.toCastMember() } ?: emptyList(),
            videos = videos?.results?.filter { it.site == "YouTube" }?.map { it.toVideo() } ?: emptyList(),
            similar = similar?.results?.map { it.toMovie() } ?: emptyList(),
            recommendations = recommendations?.results?.map { it.toMovie() } ?: emptyList()
        )
    }
}

data class TmdbTvDetailResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("overview") val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("first_air_date") val firstAirDate: String,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("vote_count") val voteCount: Int,
    @SerializedName("genres") val genres: List<TmdbGenreDto>,
    @SerializedName("number_of_seasons") val numberOfSeasons: Int,
    @SerializedName("number_of_episodes") val numberOfEpisodes: Int,
    @SerializedName("seasons") val seasons: List<TmdbSeasonDto>?,
    @SerializedName("external_ids") val externalIds: TmdbExternalIds?,
    @SerializedName("credits") val credits: TmdbCreditsDto?,
    @SerializedName("videos") val videos: TmdbVideosDto?,
    @SerializedName("similar") val similar: TmdbTvListResponse?
) {
    fun toTvDetails(): TvDetails {
        val show = TvShow(
            id = id,
            name = name,
            overview = overview,
            posterPath = posterPath,
            backdropPath = backdropPath,
            firstAirDate = firstAirDate,
            voteAverage = voteAverage,
            voteCount = voteCount,
            genreIds = genres.map { it.id },
            genres = genres.map { it.name },
            numberOfSeasons = numberOfSeasons,
            numberOfEpisodes = numberOfEpisodes,
            imdbId = externalIds?.imdbId
        )
        return TvDetails(
            tvShow = show,
            cast = credits?.cast?.take(20)?.map { it.toCastMember() } ?: emptyList(),
            videos = videos?.results?.filter { it.site == "YouTube" }?.map { it.toVideo() } ?: emptyList(),
            seasons = seasons?.map { it.toSeason() } ?: emptyList(),
            similar = similar?.results?.map { it.toTvShow() } ?: emptyList()
        )
    }
}

data class TmdbTvListResponse(
    @SerializedName("results") val results: List<TmdbTvDto>
)

data class TmdbTvDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("overview") val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("vote_count") val voteCount: Int,
    @SerializedName("genre_ids") val genreIds: List<Int>?
) {
    fun toTvShow() = TvShow(
        id = id,
        name = name,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        firstAirDate = firstAirDate ?: "",
        voteAverage = voteAverage,
        voteCount = voteCount,
        genreIds = genreIds ?: emptyList()
    )
}

data class TmdbGenreDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class TmdbExternalIds(
    @SerializedName("imdb_id") val imdbId: String?
)

data class TmdbCreditsDto(
    @SerializedName("cast") val cast: List<TmdbCastDto>?
)

data class TmdbCastDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("character") val character: String,
    @SerializedName("profile_path") val profilePath: String?,
    @SerializedName("order") val order: Int
) {
    fun toCastMember() = CastMember(
        id = id,
        name = name,
        character = character,
        profilePath = profilePath,
        order = order
    )
}

data class TmdbVideosDto(
    @SerializedName("results") val results: List<TmdbVideoDto>
)

data class TmdbVideoDto(
    @SerializedName("id") val id: String,
    @SerializedName("key") val key: String,
    @SerializedName("name") val name: String,
    @SerializedName("site") val site: String,
    @SerializedName("type") val type: String
) {
    fun toVideo() = Video(id = id, key = key, name = name, site = site, type = type)
}

data class TmdbSeasonDto(
    @SerializedName("id") val id: Int,
    @SerializedName("season_number") val seasonNumber: Int,
    @SerializedName("episode_count") val episodeCount: Int,
    @SerializedName("name") val name: String,
    @SerializedName("overview") val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("air_date") val airDate: String?
) {
    fun toSeason() = Season(
        id = id,
        seasonNumber = seasonNumber,
        episodeCount = episodeCount,
        name = name,
        overview = overview,
        posterPath = posterPath,
        airDate = airDate
    )
}

data class TmdbSearchResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val results: List<TmdbMovieDto>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
)

data class TmdbGenreListResponse(
    @SerializedName("genres") val genres: List<TmdbGenreDto>
)
