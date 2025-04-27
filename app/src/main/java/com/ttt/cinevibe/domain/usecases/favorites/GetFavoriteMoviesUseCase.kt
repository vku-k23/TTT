package com.ttt.cinevibe.domain.usecases.favorites

import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.repository.FavoriteMovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteMoviesUseCase @Inject constructor(
    private val favoriteMovieRepository: FavoriteMovieRepository
) {
    operator fun invoke(): Flow<List<Movie>> {
        return favoriteMovieRepository.getAllFavoriteMovies()
    }
}