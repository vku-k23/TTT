package com.ttt.cinevibe.domain.usecases.movies

import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPopularMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    operator fun invoke(): Flow<List<Movie>> {
        return movieRepository.getPopularMovies()
    }
}