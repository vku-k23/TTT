package com.ttt.cinevibe.data.remote.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class UserResponse(
    val firebaseUid: String,
    val displayName: String,
    val username: String,
    val email: String,
    val profileImageUrl: String? = null,
    val bio: String? = null,
    val favoriteGenre: String? = null,
    val reviewCount: Int? = null,
    val followersCount: Int? = null,
    val followingCount: Int? = null,
    val createdAt: String? = null, 
    val lastLogin: String? = null 
)