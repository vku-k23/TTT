package com.ttt.cinevibe.data.remote.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.ttt.cinevibe.domain.model.TvSeries
import com.ttt.cinevibe.domain.model.Episode

@Serializable
data class TvSeriesDto(
    val id: Int,
    val name: String,
    val overview: String? = null,
    @SerialName("first_air_date")
    val firstAirDate: String? = null,
    @SerialName("poster_path")
    val posterPath: String? = null,
    @SerialName("backdrop_path")
    val backdropPath: String? = null,
    @SerialName("vote_average")
    val voteAverage: Double,
    @SerialName("vote_count")
    val voteCount: Int,
    val popularity: Double,
    @SerialName("original_name")
    val originalName: String,
    @SerialName("original_language")
    val originalLanguage: String,
    @SerialName("genre_ids")
    val genreIds: List<Int>,
    @SerialName("number_of_seasons")
    val numberOfSeasons: Int? = null,
    @SerialName("number_of_episodes")
    val numberOfEpisodes: Int? = null,
    val status: String? = null,
    @SerialName("in_production")
    val inProduction: Boolean? = null,
    val tagline: String? = null,
    val type: String? = null,
    @SerialName("next_episode_to_air")
    val nextEpisodeToAir: EpisodeDto? = null,
    @SerialName("last_episode_to_air")
    val lastEpisodeToAir: EpisodeDto? = null
) {
    fun toDomainModel(): TvSeries = TvSeries(
        id = id,
        name = name,
        overview = overview,
        firstAirDate = firstAirDate,
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        originalName = originalName,
        originalLanguage = originalLanguage,
        genreIds = genreIds,
        numberOfSeasons = numberOfSeasons,
        numberOfEpisodes = numberOfEpisodes,
        status = status,
        inProduction = inProduction,
        tagline = tagline,
        type = type,
        nextEpisodeToAir = nextEpisodeToAir?.toDomainModel(),
        lastEpisodeToAir = lastEpisodeToAir?.toDomainModel()
    )
}

@Serializable
data class EpisodeDto(
    val id: Int,
    val name: String,
    val overview: String? = null,
    @SerialName("air_date")
    val airDate: String? = null,
    @SerialName("episode_number")
    val episodeNumber: Int,
    @SerialName("season_number")
    val seasonNumber: Int,
    @SerialName("still_path")
    val stillPath: String? = null
) {
    fun toDomainModel(): Episode = Episode(
        id = id,
        name = name,
        overview = overview,
        airDate = airDate,
        episodeNumber = episodeNumber,
        seasonNumber = seasonNumber,
        stillPath = stillPath
    )
}

@Serializable
data class TvSeriesResponse(
    val page: Int,
    val results: List<TvSeriesDto>,
    @SerialName("total_pages")
    val totalPages: Int,
    @SerialName("total_results")
    val totalResults: Int
) 