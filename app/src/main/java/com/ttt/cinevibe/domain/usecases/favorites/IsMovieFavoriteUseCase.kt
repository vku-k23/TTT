package com.ttt.cinevibe.domain.usecases.favorites

import com.ttt.cinevibe.domain.repository.FavoriteMovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsMovieFavoriteUseCase @Inject constructor(
    private val favoriteMovieRepository: FavoriteMovieRepository
) {
    operator fun invoke(movieId: Int): Flow<Boolean> {
        return favoriteMovieRepository.isMovieFavorite(movieId)
    }
}