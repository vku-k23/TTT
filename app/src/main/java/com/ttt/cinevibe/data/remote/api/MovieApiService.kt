package com.ttt.cinevibe.data.remote.api

import com.ttt.cinevibe.data.remote.models.GenreResponse
import com.ttt.cinevibe.data.remote.models.MovieDetailResponse
import com.ttt.cinevibe.data.remote.models.MovieListResponse
import com.ttt.cinevibe.data.remote.models.VideoResponse
import com.ttt.cinevibe.data.remote.models.TvSeriesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieListResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieListResponse

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("language") language: String = "en-US"
    ): MovieListResponse

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieListResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): MovieListResponse

    @GET("genre/movie/list")
    suspend fun getGenres(
        @Query("language") language: String = "en-US"
    ): GenreResponse
    
    @GET("movie/{movieId}")
    suspend fun getMovieDetails(
        @Path("movieId") movieId: Int,
        @Query("language") language: String = "en-US"
    ): MovieDetailResponse
    
    @GET("movie/{movieId}/videos")
    suspend fun getMovieVideos(
        @Path("movieId") movieId: Int,
        @Query("language") language: String = "en-US"
    ): VideoResponse
    
    @GET("movie/{movieId}/similar")
    suspend fun getSimilarMovies(
        @Path("movieId") movieId: Int,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieListResponse

    @GET("tv/popular")
    suspend fun getPopularTvSeries(
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null
    ): TvSeriesResponse

    @GET("tv/top_rated")
    suspend fun getTopRatedTvSeries(
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null
    ): TvSeriesResponse

    @GET("tv/on_the_air")
    suspend fun getOnTheAirTvSeries(
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null
    ): TvSeriesResponse

    @GET("tv/{series_id}")
    suspend fun getTvSeriesDetails(
        @Path("series_id") seriesId: Int,
        @Query("language") language: String? = null
    ): TvSeriesResponse

    @GET("tv/{series_id}/videos")
    suspend fun getTvSeriesVideos(
        @Path("series_id") seriesId: Int,
        @Query("language") language: String? = null
    ): VideoResponse

    @GET("search/tv")
    suspend fun searchTvSeries(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String? = null
    ): TvSeriesResponse
}