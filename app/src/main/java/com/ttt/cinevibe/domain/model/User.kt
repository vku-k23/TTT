package com.ttt.cinevibe.domain.model

data class User(
    val id: Int,
    val username: String,
    val displayName: String,
    val avatar: String,
    val bio: String,
    val followersCount: Int = 0,
    val followingCount: Int = 0
)