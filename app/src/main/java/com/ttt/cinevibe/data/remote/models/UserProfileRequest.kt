package com.ttt.cinevibe.data.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileRequest(
    val firebaseUid: String? = null,
    val displayName: String? = null,
    val username: String? = null,
    val profileImageUrl: String? = null,
    val bio: String? = null,
    val favoriteGenre: String? = null,
    val reviewCount: Int? = null,
    val followersCount: Int? = null,
    val followingCount: Int? = null
)