package com.ttt.cinevibe.domain.model

data class TvSeries(
    val id: Int,
    val name: String,
    val overview: String?,
    val firstAirDate: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val popularity: Double,
    val originalName: String,
    val originalLanguage: String,
    val genreIds: List<Int>,
    val numberOfSeasons: Int? = null,
    val numberOfEpisodes: Int? = null,
    val status: String? = null,
    val inProduction: Boolean? = null,
    val tagline: String? = null,
    val type: String? = null,
    val nextEpisodeToAir: Episode? = null,
    val lastEpisodeToAir: Episode? = null
)

data class Episode(
    val id: Int,
    val name: String,
    val overview: String?,
    val airDate: String?,
    val episodeNumber: Int,
    val seasonNumber: Int,
    val stillPath: String?
) 