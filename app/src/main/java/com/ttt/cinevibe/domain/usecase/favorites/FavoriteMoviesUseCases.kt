package com.ttt.cinevibe.domain.usecase.favorites

data class FavoriteMoviesUseCases(
    val getFavoriteMovies: GetFavoriteMoviesUseCase,
    val addMovieToFavorites: AddMovieToFavoritesUseCase,
    val removeMovieFromFavorites: RemoveMovieFromFavoritesUseCase,
    val isMovieFavorite: IsMovieFavoriteUseCase
)