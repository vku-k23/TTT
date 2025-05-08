package com.ttt.cinevibe.domain.usecase.favorites

import com.ttt.cinevibe.domain.repository.FavoriteMovieRepository
import javax.inject.Inject

class RemoveMovieFromFavoritesUseCase @Inject constructor(
    private val favoriteMovieRepository: FavoriteMovieRepository
) {
    suspend operator fun invoke(movieId: Int) {
        favoriteMovieRepository.removeMovieFromFavorites(movieId)
    }
}