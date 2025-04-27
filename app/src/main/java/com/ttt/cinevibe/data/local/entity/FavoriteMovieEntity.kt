package com.ttt.cinevibe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ttt.cinevibe.domain.model.Movie

@Entity(tableName = "favorite_movies")
data class FavoriteMovieEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val genres: String, // Store list as comma-separated string
    val trailerVideoKey: String?,
    val addedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromMovie(movie: Movie): FavoriteMovieEntity {
            return FavoriteMovieEntity(
                id = movie.id,
                title = movie.title,
                overview = movie.overview,
                posterPath = movie.posterPath,
                backdropPath = movie.backdropPath,
                releaseDate = movie.releaseDate,
                voteAverage = movie.voteAverage,
                genres = movie.genres.joinToString(","),
                trailerVideoKey = movie.trailerVideoKey
            )
        }
        
        fun toMovie(entity: FavoriteMovieEntity): Movie {
            return Movie(
                id = entity.id,
                title = entity.title,
                overview = entity.overview,
                posterPath = entity.posterPath,
                backdropPath = entity.backdropPath,
                releaseDate = entity.releaseDate,
                voteAverage = entity.voteAverage,
                genres = entity.genres.split(",").filter { it.isNotEmpty() },
                trailerVideoKey = entity.trailerVideoKey
            )
        }
    }
}