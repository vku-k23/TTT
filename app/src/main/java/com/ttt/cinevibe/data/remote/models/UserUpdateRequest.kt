package com.ttt.cinevibe.data.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateRequest(
    val displayName: String? = null,
    val profileImageUrl: String? = null,
    val bio: String? = null,
    val favoriteGenre: String? = null
)