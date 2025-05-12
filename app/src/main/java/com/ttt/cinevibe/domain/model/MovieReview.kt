package com.ttt.cinevibe.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MovieReview(
    val id: Long,
    val tmdbMovieId: Long,
    val rating: Int,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val likeCount: Int = 0,
    val userProfile: UserProfile,
    val userHasLiked: Boolean = false
) {
    // Format date for display
    fun getFormattedDate(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            
            val date = inputFormat.parse(createdAt)
            if (date != null) {
                outputFormat.format(date)
            } else {
                createdAt.substringBefore('T')
            }
        } catch (e: Exception) {
            // Fallback if date parsing fails
            createdAt.substringBefore('T')
        }
    }
}

data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String? = null,
    val avatarUrl: String? = null
)