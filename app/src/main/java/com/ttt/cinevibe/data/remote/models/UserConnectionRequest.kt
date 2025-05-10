package com.ttt.cinevibe.data.remote.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class UserConnectionRequest(
    @SerializedName("targetUserUid")
    val targetUserUid: String
)