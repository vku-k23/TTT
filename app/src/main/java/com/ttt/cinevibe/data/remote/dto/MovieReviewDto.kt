package com.ttt.cinevibe.data.remote.dto

import com.ttt.cinevibe.domain.model.MovieReview
import com.ttt.cinevibe.domain.model.UserProfile
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class MovieReviewDto(
    val id: Long,
    val tmdbMovieId: Long,
    val userId: String? = null,
    val rating: Float,
    val content: String? = null,
    @SerialName("reviewText")
    val reviewText: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val likeCount: Int = 0,
    val userProfile: UserProfileDto? = null,
    val userHasLiked: Boolean = false,
    val userUid: String? = null,
    val userName: String? = null,
    val userProfileImageUrl: String? = null
) {
    fun toMovieReview(): MovieReview {
        val profile = when {
            userProfile != null -> userProfile.toUserProfile()
            
            userUid != null || userName != null -> UserProfile(
                uid = userUid ?: userId ?: "",
                displayName = userName ?: "Guest User",
                email = null,
                avatarUrl = userProfileImageUrl
            )
            
            userId != null -> UserProfile(
                uid = userId,
                displayName = "User",
                email = null,
                avatarUrl = null
            )
            
            else -> UserProfile.empty()
        }
        
        val reviewContent = reviewText ?: content ?: ""
        
        // Log giá trị likeCount
        android.util.Log.d("MovieReviewDto", "Converting DTO to MovieReview: " +
                "id=$id, likeCount=$likeCount, userHasLiked=$userHasLiked")
        
        return MovieReview(
            id = id,
            tmdbMovieId = tmdbMovieId,
            rating = rating,
            content = reviewContent,
            createdAt = createdAt,
            updatedAt = updatedAt,
            likeCount = likeCount,
            userProfile = profile,
            userHasLiked = userHasLiked
        )
    }
}

@Serializable
data class UserProfileDto(
    val uid: String,
    val displayName: String,
    val email: String? = null,
    val avatarUrl: String? = null
) {
    fun toUserProfile(): UserProfile {
        return UserProfile(
            uid = uid,
            displayName = displayName,
            email = email,
            avatarUrl = avatarUrl
        )
    }
}

@Serializable
data class CreateReviewRequest(
    val tmdbMovieId: Long,
    val movieTitle: String,
    @SerialName("reviewText")
    val reviewText: String = "",
    val rating: Float,
    val containsSpoilers: Boolean = false
)

@Serializable
data class UpdateReviewRequest(
    val rating: Float,
    @SerialName("reviewText")
    val reviewText: String = "",
    val containsSpoilers: Boolean = false
)

@Serializable
data class MovieReviewResponse(
    val content: List<MovieReviewDto>,
    val pageable: PageableDto,
    val last: Boolean,
    val totalPages: Int,
    val totalElements: Long,
    val first: Boolean,
    val size: Int,
    val number: Int,
    val sort: SortDto,
    val numberOfElements: Int,
    val empty: Boolean
)

@Serializable
data class PageableDto(
    val pageNumber: Int,
    val pageSize: Int,
    val sort: SortDto,
    val offset: Long,
    val paged: Boolean,
    val unpaged: Boolean
)

@Serializable
data class SortDto(
    val empty: Boolean,
    val sorted: Boolean,
    val unsorted: Boolean
)

@Serializable
data class ReviewApiResponse(
    val success: Boolean = true,
    val message: String? = null,
    val data: MovieReviewDto? = null
)

@Serializable
data class BooleanResponse(
    val success: Boolean = true,
    val message: String? = null,
    val data: Boolean? = null
)