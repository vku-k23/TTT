package com.ttt.cinevibe.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MovieReview(
    val id: Long,
    val tmdbMovieId: Long,
    val rating: Float,
    val content: String = "",
    val createdAt: String,
    val updatedAt: String,
    val likeCount: Int = 0,
    val userProfile: UserProfile,
    val userHasLiked: Boolean = false
) {
    // Format date for display
    fun getFormattedDate(): String {
        return try {
            // Try to parse ISO 8601 format with timezone
            val inputFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            )
            
            val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            
            // Try each format until one works
            for (format in inputFormats) {
                try {
                    val date = format.parse(createdAt)
                    if (date != null) {
                        return outputFormat.format(date)
                    }
                } catch (e: Exception) {
                    // Continue to next format
                }
            }
            
            // If all parsing fails, extract date part
            if (createdAt.contains('T')) {
                createdAt.substringBefore('T')
            } else {
                createdAt
            }
        } catch (e: Exception) {
            // Final fallback
            createdAt
        }
    }
}

data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String? = null,
    val avatarUrl: String? = null
) {
    companion object {
        fun empty(): UserProfile {
            return UserProfile(
                uid = "",
                displayName = "Guest User",
                email = null,
                avatarUrl = null
            )
        }
    }
    
    // Check if profile is essentially empty/invalid
    fun isEmpty(): Boolean {
        return uid.isBlank() && displayName.isBlank() && email.isNullOrBlank()
    }
}