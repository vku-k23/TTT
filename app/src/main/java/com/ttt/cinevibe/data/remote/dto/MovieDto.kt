package com.ttt.cinevibe.data.remote.dto

import com.ttt.cinevibe.domain.model.Movie
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDto(
    val id: Int,
    val title: String,
    val overview: String,
    @SerialName("poster_path")
    val posterPath: String?,
    @SerialName("backdrop_path")
    val backdropPath: String?,
    @SerialName("release_date")
    val releaseDate: String?,
    @SerialName("vote_average")
    val voteAverage: Double
) {
    fun toMovie(): Movie {
        return Movie(
            id = id,
            title = title,
            overview = overview,
            posterPath = posterPath,
            backdropPath = backdropPath,
            releaseDate = releaseDate,
            voteAverage = voteAverage
        )
    }
}