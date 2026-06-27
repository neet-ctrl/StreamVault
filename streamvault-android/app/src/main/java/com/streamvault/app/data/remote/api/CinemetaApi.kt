package com.streamvault.app.data.remote.api

import com.streamvault.app.data.remote.dto.CinemetaCatalogResponse
import com.streamvault.app.data.remote.dto.CinemetaMetaResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface CinemetaApi {

    @GET("catalog/movie/top.json")
    suspend fun getTopMovies(): CinemetaCatalogResponse

    @GET("catalog/series/top.json")
    suspend fun getTopSeries(): CinemetaCatalogResponse

    /**
     * Full URL built by the repository:
     * https://v3-cinemeta.strem.io/catalog/{type}/top/search={encodedQuery}.json
     */
    @GET
    suspend fun search(@Url url: String): CinemetaCatalogResponse

    @GET("meta/movie/{id}.json")
    suspend fun getMovieMeta(@Path("id") id: String): CinemetaMetaResponse

    @GET("meta/series/{id}.json")
    suspend fun getSeriesMeta(@Path("id") id: String): CinemetaMetaResponse
}
