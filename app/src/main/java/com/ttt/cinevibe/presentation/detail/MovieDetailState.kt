package com.ttt.cinevibe.presentation.detail

import com.ttt.cinevibe.domain.model.Movie

/**
 * Represents the UI state for the movie detail screen
 */
data class MovieDetailState(
    val movie: Movie? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)