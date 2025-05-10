package com.ttt.cinevibe.data.remote.dto

import com.ttt.cinevibe.domain.model.MovieReview
import com.ttt.cinevibe.domain.model.UserProfile
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class MovieReviewDto(
    val id: Long,
    val tmdbMovieId: Long,
    val userId: String,
    val rating: Int,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val likeCount: Int = 0,
    val userProfile: UserProfileDto,
    val userHasLiked: Boolean = false
) {
    fun toMovieReview(): MovieReview {
        return MovieReview(
            id = id,
            tmdbMovieId = tmdbMovieId,
            rating = rating,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt,
            likeCount = likeCount,
            userProfile = userProfile.toUserProfile(),
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
    val rating: Int,
    val content: String
)

@Serializable
data class UpdateReviewRequest(
    val rating: Int,
    val content: String
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
    val success: Boolean,
    val message: String? = null,
    val data: MovieReviewDto? = null
)

@Serializable
data class BooleanResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Boolean? = null
)