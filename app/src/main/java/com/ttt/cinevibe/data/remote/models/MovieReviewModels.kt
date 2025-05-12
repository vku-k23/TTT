package com.ttt.cinevibe.data.remote.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

// Request to create or update a review
data class MovieReviewRequest(
    @SerializedName("tmdbMovieId")
    val tmdbMovieId: Long,
    
    @SerializedName("rating")
    val rating: Int,
    
    @SerializedName("content")
    val content: String
)

// Response from the API containing review data
data class MovieReviewResponse(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("tmdbMovieId")
    val tmdbMovieId: Long,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("rating")
    val rating: Int,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String,
    
    @SerializedName("userProfile")
    val userProfile: UserProfileResponse,
    
    @SerializedName("likeCount")
    val likeCount: Int,
    
    @SerializedName("userHasLiked")
    val userHasLiked: Boolean
)

// Paginated response wrapper for review lists
data class PagedMovieReviewsResponse(
    @SerializedName("content")
    val content: List<MovieReviewResponse>,
    
    @SerializedName("totalElements")
    val totalElements: Int,
    
    @SerializedName("totalPages")
    val totalPages: Int,
    
    @SerializedName("size")
    val size: Int,
    
    @SerializedName("number")
    val number: Int,
    
    @SerializedName("numberOfElements")
    val numberOfElements: Int,
    
    @SerializedName("first")
    val first: Boolean,
    
    @SerializedName("last")
    val last: Boolean,
    
    @SerializedName("empty")
    val empty: Boolean
)

// Model for has-reviewed check response
data class HasReviewedResponse(
    @SerializedName("hasReviewed")
    val hasReviewed: Boolean
)