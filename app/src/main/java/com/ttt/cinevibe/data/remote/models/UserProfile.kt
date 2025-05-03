package com.ttt.cinevibe.data.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val firebaseUid: String,
    val displayName: String,
    val profileImageUrl: String? = null,
    val bio: String? = null,
    val favoriteGenre: String? = null,
    val reviewCount: Int? = null,
    val followersCount: Int? = null,
    val followingCount: Int? = null
)