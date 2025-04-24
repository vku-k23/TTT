package com.ttt.cinevibe.domain.repository

import com.ttt.cinevibe.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getPopularMovies(): Flow<List<Movie>>
    suspend fun getTopRatedMovies(): Flow<List<Movie>>
    suspend fun getTrendingMovies(): Flow<List<Movie>>
    suspend fun getUpcomingMovies(): Flow<List<Movie>>
    suspend fun searchMovies(query: String): Flow<List<Movie>>
    suspend fun getGenres(): Flow<Map<Int, String>>
}