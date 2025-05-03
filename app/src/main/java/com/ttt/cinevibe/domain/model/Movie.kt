package com.ttt.cinevibe.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val genres: List<String> = emptyList(),
    val trailerVideoKey: String? = null  // YouTube video key for the trailer
)