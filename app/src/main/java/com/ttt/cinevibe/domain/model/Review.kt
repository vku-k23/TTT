package com.ttt.cinevibe.domain.model

data class Review(
    val id: Long,
    val userUid: String,
    val userName: String,
    val userProfileImageUrl: String?,
    val tmdbMovieId: Long,
    val movieTitle: String,
    val rating: Float,
    val reviewText: String,
    val containsSpoilers: Boolean = false,
    val likesCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: String,
    val updatedAt: String? = null
)