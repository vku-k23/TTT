package com.ttt.cinevibe.data.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String
)