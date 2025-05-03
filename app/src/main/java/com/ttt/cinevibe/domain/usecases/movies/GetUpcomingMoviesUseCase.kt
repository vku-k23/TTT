package com.ttt.cinevibe.domain.usecases.movies

import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUpcomingMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    suspend operator fun invoke(language: String? = null): Flow<List<Movie>> {
        return movieRepository.getUpcomingMovies(language)
    }
}