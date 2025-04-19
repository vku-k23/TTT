package com.ttt.cinevibe.data.repository

import com.ttt.cinevibe.data.remote.MovieApi
import com.ttt.cinevibe.data.remote.dto.toMovie
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.repository.MovieRepository
import com.ttt.cinevibe.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val movieApi: MovieApi
) : MovieRepository {

    override fun getPopularMovies(): Flow<List<Movie>> = flow {
        try {
            val response = movieApi.getPopularMovies(apiKey = Constants.API_KEY)
            emit(response.movies.map { it.toMovie() })
        } catch (e: Exception) {
            // TODO: Handle exceptions
            emit(emptyList())
        }
    }

    override fun searchMovies(query: String): Flow<List<Movie>> = flow {
        try {
            val response = movieApi.searchMovies(apiKey = Constants.API_KEY, query = query)
            emit(response.movies.map { it.toMovie() })
        } catch (e: Exception) {
            // TODO: Handle exceptions
            emit(emptyList())
        }
    }
}