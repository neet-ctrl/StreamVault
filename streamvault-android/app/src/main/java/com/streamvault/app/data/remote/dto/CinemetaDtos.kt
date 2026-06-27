package com.streamvault.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.streamvault.app.domain.model.*

// ─── Catalog response ────────────────────────────────────────────────────────

data class CinemetaCatalogResponse(
    @SerializedName("metas") val metas: List<CinemetaMetaDto>? = null
)

// ─── Meta (detail) response ───────────────────────────────────────────────────

data class CinemetaMetaResponse(
    @SerializedName("meta") val meta: CinemetaDetailDto? = null
)

// ─── Lightweight catalog item ─────────────────────────────────────────────────

data class CinemetaMetaDto(
    @SerializedName("id")          val id: String          = "",
    @SerializedName("type")        val type: String        = "movie",
    @SerializedName("name")        val name: String        = "",
    @SerializedName("poster")      val poster: String?     = null,
    @SerializedName("background")  val background: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("releaseInfo") val releaseInfo: String? = null,
    @SerializedName("imdbRating")  val imdbRating: String? = null,
    @SerializedName("genres")      val genres: List<String>? = null
) {
    fun toMovie() = Movie(
        id          = id,
        title       = name,
        overview    = description ?: "",
        posterPath  = poster,
        backdropPath = background,
        releaseDate = releaseInfo ?: "",
        voteAverage = imdbRating?.toDoubleOrNull() ?: 0.0,
        voteCount   = 0,
        genres      = genres ?: emptyList(),
        imdbId      = id,
        mediaType   = if (type == "series") MediaType.TV else MediaType.MOVIE
    )

    fun toTvShow() = TvShow(
        id           = id,
        name         = name,
        overview     = description ?: "",
        posterPath   = poster,
        backdropPath = background,
        firstAirDate = releaseInfo ?: "",
        voteAverage  = imdbRating?.toDoubleOrNull() ?: 0.0,
        voteCount    = 0,
        genres       = genres ?: emptyList(),
        imdbId       = id,
        mediaType    = MediaType.TV
    )
}

// ─── Full detail item ─────────────────────────────────────────────────────────

data class CinemetaDetailDto(
    @SerializedName("id")          val id: String          = "",
    @SerializedName("type")        val type: String        = "movie",
    @SerializedName("name")        val name: String        = "",
    @SerializedName("poster")      val poster: String?     = null,
    @SerializedName("background")  val background: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("releaseInfo") val releaseInfo: String? = null,
    @SerializedName("imdbRating")  val imdbRating: String? = null,
    @SerializedName("runtime")     val runtime: String?    = null,
    @SerializedName("genres")      val genres: List<String>? = null,
    @SerializedName("cast")        val cast: List<String>?   = null,
    @SerializedName("director")    val director: List<String>? = null,
    @SerializedName("writer")      val writer: List<String>?   = null,
    @SerializedName("country")     val country: String?    = null,
    @SerializedName("language")    val language: String?   = null,
    @SerializedName("awards")      val awards: String?     = null,
    @SerializedName("videos")      val videos: List<CinemetaVideoDto>? = null
) {
    /** Parse "120 min" or "1h 30m" → integer minutes. */
    private val runtimeMinutes: Int?
        get() = runtime?.filter { it.isDigit() || it == ' ' }
            ?.trim()
            ?.split(" ")
            ?.firstOrNull()
            ?.toIntOrNull()

    private fun baseMovie() = Movie(
        id           = id,
        title        = name,
        overview     = description ?: "",
        posterPath   = poster,
        backdropPath = background,
        releaseDate  = releaseInfo ?: "",
        voteAverage  = imdbRating?.toDoubleOrNull() ?: 0.0,
        voteCount    = 0,
        genres       = genres ?: emptyList(),
        runtime      = runtimeMinutes,
        imdbId       = id,
        mediaType    = if (type == "series") MediaType.TV else MediaType.MOVIE
    )

    private fun baseTvShow() = TvShow(
        id           = id,
        name         = name,
        overview     = description ?: "",
        posterPath   = poster,
        backdropPath = background,
        firstAirDate = releaseInfo ?: "",
        voteAverage  = imdbRating?.toDoubleOrNull() ?: 0.0,
        voteCount    = 0,
        genres       = genres ?: emptyList(),
        imdbId       = id,
        mediaType    = MediaType.TV
    )

    private fun castMembers(): List<CastMember> =
        cast?.mapIndexed { i, name ->
            CastMember(id = i, name = name, character = "", profilePath = null, order = i)
        } ?: emptyList()

    fun toMovieDetails() = MovieDetails(
        movie           = baseMovie(),
        cast            = castMembers(),
        videos          = emptyList(),
        similar         = emptyList(),
        recommendations = emptyList()
    )

    fun toTvDetails(): TvDetails {
        // Group Cinemeta episode videos into Season domain objects
        val seasonMap = videos
            ?.filter { it.season != null && it.episode != null }
            ?.groupBy { it.season!! }
            ?: emptyMap()

        val seasons = seasonMap.entries.sortedBy { it.key }.map { (seasonNum, eps) ->
            Season(
                id           = seasonNum,
                seasonNumber = seasonNum,
                episodeCount = eps.size,
                name         = "Season $seasonNum",
                overview     = "",
                posterPath   = null,
                airDate      = eps.minByOrNull { it.episode ?: 0 }?.released
            )
        }

        return TvDetails(
            tvShow  = baseTvShow(),
            cast    = castMembers(),
            videos  = emptyList(),
            seasons = seasons,
            similar = emptyList()
        )
    }
}

// ─── Episode entry inside a series meta response ──────────────────────────────

data class CinemetaVideoDto(
    @SerializedName("id")       val id: String      = "",
    @SerializedName("title")    val title: String?  = null,
    @SerializedName("season")   val season: Int?    = null,
    @SerializedName("episode")  val episode: Int?   = null,
    @SerializedName("overview") val overview: String? = null,
    @SerializedName("thumbnail") val thumbnail: String? = null,
    @SerializedName("released") val released: String? = null
)
