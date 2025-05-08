package com.ttt.cinevibe.domain.usecase.movies

import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetSimilarMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    operator fun invoke(movieId: Int, language: String? = null): Flow<List<Movie>> = flow {
        try {
            movieRepository.getSimilarMovies(movieId, language).collect { movies ->
                emit(movies)
            }
        } catch (e: Exception) {
            // If repository call fails, emit an empty list as fallback
            emit(emptyList())
        }
    }
}