package com.ttt.cinevibe.domain.repository

import com.ttt.cinevibe.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface FavoriteMovieRepository {
    fun getAllFavoriteMovies(): Flow<List<Movie>>
    suspend fun addMovieToFavorites(movie: Movie)
    suspend fun removeMovieFromFavorites(movieId: Int)
    fun isMovieFavorite(movieId: Int): Flow<Boolean>
}