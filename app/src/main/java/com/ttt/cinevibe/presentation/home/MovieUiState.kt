package com.ttt.cinevibe.presentation.home

import com.ttt.cinevibe.domain.model.Movie

data class MovieUiState(
    val isLoading: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val error: String? = null
)
