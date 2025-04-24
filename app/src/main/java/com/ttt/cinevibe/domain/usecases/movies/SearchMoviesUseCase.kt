package com.ttt.cinevibe.domain.usecases.movies

import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    suspend operator fun invoke(query: String): Flow<List<Movie>> {
        if (query.isBlank()) {
            // If query is blank, return popular movies instead
            return movieRepository.getPopularMovies()
        }
        return movieRepository.searchMovies(query)
    }
}