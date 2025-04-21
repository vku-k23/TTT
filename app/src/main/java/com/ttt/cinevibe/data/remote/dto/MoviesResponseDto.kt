package com.ttt.cinevibe.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MoviesResponseDto(
    val page: Int,
    @SerializedName("results")
    val movies: List<MovieDto>,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)