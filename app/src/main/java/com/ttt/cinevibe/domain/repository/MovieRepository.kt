package com.ttt.cinevibe.domain.repository

import com.ttt.cinevibe.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {

    fun getPopularMovies(): Flow<List<Movie>>

    fun searchMovies(query: String): Flow<List<Movie>>

}