package com.ttt.cinevibe.data.remote.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoResponse(
    val id: Int,
    val results: List<VideoDto>
)

@Serializable
data class VideoDto(
    val id: String,
    val name: String,
    val key: String,
    val site: String,
    val size: Int,
    val type: String,
    @SerialName("official")
    val official: Boolean,
    @SerialName("published_at")
    val publishedAt: String
)