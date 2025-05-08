package com.ttt.cinevibe.domain.usecase.favorites

import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.repository.FavoriteMovieRepository
import javax.inject.Inject

class AddMovieToFavoritesUseCase @Inject constructor(
    private val favoriteMovieRepository: FavoriteMovieRepository
) {
    suspend operator fun invoke(movie: Movie) {
        favoriteMovieRepository.addMovieToFavorites(movie)
    }
}