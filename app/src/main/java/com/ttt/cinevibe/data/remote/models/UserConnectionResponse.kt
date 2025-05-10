package com.ttt.cinevibe.data.remote.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import java.util.Date

@Serializable
data class UserConnectionResponse(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("followerUid")
    val followerUid: String,
    
    @SerializedName("followerName")
    val followerName: String,
    
    @SerializedName("followerProfileImageUrl")
    val followerProfileImageUrl: String?,
    
    @SerializedName("followingUid")
    val followingUid: String,
    
    @SerializedName("followingName")
    val followingName: String,
    
    @SerializedName("followingProfileImageUrl")
    val followingProfileImageUrl: String?,
    
    @SerializedName("status")
    val status: String, // Can be "PENDING" or "ACCEPTED"
    
    @SerializedName("createdAt")
    @Serializable(with = DateSerializer::class)
    val createdAt: Date
)