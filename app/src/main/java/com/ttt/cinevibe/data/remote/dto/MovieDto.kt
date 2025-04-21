package com.ttt.cinevibe.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.ttt.cinevibe.domain.model.Movie

data class MovieDto(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double
) {
    fun toMovie(): Movie {
        return Movie(
            id = id,
            title = title,
            overview = overview,
            posterPath = posterPath,
            releaseDate = releaseDate,
            voteAverage = voteAverage
        )
    }
}