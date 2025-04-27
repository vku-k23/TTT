package com.ttt.cinevibe.domain.usecases.favorites

data class FavoriteMoviesUseCases(
    val getFavoriteMovies: GetFavoriteMoviesUseCase,
    val addMovieToFavorites: AddMovieToFavoritesUseCase,
    val removeMovieFromFavorites: RemoveMovieFromFavoritesUseCase,
    val isMovieFavorite: IsMovieFavoriteUseCase
)