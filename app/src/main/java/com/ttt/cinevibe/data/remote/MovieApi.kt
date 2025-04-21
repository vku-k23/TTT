package com.ttt.cinevibe.data.remote

import com.ttt.cinevibe.data.remote.dto.MovieDto
import com.ttt.cinevibe.data.remote.dto.MoviesResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApi {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): MoviesResponseDto

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): MoviesResponseDto
}