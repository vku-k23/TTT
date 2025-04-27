package com.ttt.cinevibe.data.repository

import com.ttt.cinevibe.data.local.dao.FavoriteMovieDao
import com.ttt.cinevibe.data.local.entity.FavoriteMovieEntity
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.repository.FavoriteMovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteMovieRepositoryImpl @Inject constructor(
    private val favoriteMovieDao: FavoriteMovieDao
) : FavoriteMovieRepository {
    
    override fun getAllFavoriteMovies(): Flow<List<Movie>> {
        return favoriteMovieDao.getAllFavorites()
            .map { entities ->
                entities.map { FavoriteMovieEntity.toMovie(it) }
            }
    }

    override suspend fun addMovieToFavorites(movie: Movie) {
        val entity = FavoriteMovieEntity.fromMovie(movie)
        favoriteMovieDao.addToFavorites(entity)
    }

    override suspend fun removeMovieFromFavorites(movieId: Int) {
        favoriteMovieDao.removeFromFavorites(movieId)
    }

    override fun isMovieFavorite(movieId: Int): Flow<Boolean> {
        return favoriteMovieDao.isMovieFavorite(movieId)
    }
}