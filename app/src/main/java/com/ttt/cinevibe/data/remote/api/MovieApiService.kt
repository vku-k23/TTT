package com.ttt.cinevibe.data.remote.api

import com.ttt.cinevibe.data.remote.models.GenreResponse
import com.ttt.cinevibe.data.remote.models.MovieListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApiService {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieListResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieListResponse

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): MovieListResponse

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieListResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): MovieListResponse

    @GET("genre/movie/list")
    suspend fun getGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): GenreResponse
}