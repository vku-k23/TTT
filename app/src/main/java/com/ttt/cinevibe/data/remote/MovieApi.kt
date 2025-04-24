package com.ttt.cinevibe.data.remote

import com.ttt.cinevibe.data.remote.dto.MovieDto
import com.ttt.cinevibe.data.remote.dto.MoviesResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApi {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1
    ): MoviesResponseDto

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): MoviesResponseDto
}