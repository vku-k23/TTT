package com.ttt.cinevibe.domain.repository

import com.ttt.cinevibe.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getPopularMovies(language: String? = null): Flow<List<Movie>>
    suspend fun getTopRatedMovies(language: String? = null): Flow<List<Movie>>
    suspend fun getTrendingMovies(language: String? = null): Flow<List<Movie>>
    suspend fun getUpcomingMovies(language: String? = null): Flow<List<Movie>>
    suspend fun searchMovies(query: String, language: String? = null): Flow<List<Movie>>
    suspend fun getGenres(): Flow<Map<Int, String>>
    suspend fun getMovieById(movieId: Int, language: String? = null): Flow<Movie> // Already has language parameter
}