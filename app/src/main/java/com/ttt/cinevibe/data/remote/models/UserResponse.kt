package com.ttt.cinevibe.data.remote.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class UserResponse(
    val firebaseUid: String,
    val displayName: String,
    val email: String,
    val profileImageUrl: String? = null,
    val bio: String? = null,
    val favoriteGenre: String? = null,
    val createdAt: String? = null, // Using String for serialization compatibility
    val lastLogin: String? = null  // Using String for serialization compatibility
)