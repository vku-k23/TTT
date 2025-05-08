package com.ttt.cinevibe.domain.model

data class Review(
    val id: Int,
    val userId: Int,
    val userName: String,
    val userAvatar: String,
    val movieId: Int,
    val movieTitle: String,
    val rating: Float,
    val content: String,
    val likes: Int = 0,
    val comments: Int = 0,
    val timestamp: Long
)