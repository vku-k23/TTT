package com.ttt.cinevibe.domain.model

data class MovieList(
    val id: Int,
    val userId: Int,
    val userName: String,
    val title: String,
    val description: String,
    val movieCount: Int = 0,
    val likes: Int = 0,
    val isPublic: Boolean = true,
    val createdAt: Long
)