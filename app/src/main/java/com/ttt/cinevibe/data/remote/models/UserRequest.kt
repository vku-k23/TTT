package com.ttt.cinevibe.data.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class UserRequest(
    val firebaseUid: String,
    val email: String,
    val displayName: String,
    val username: String
)