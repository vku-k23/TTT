package com.ttt.cinevibe.data.remote.dto

import com.ttt.cinevibe.domain.model.Comment
import com.ttt.cinevibe.domain.model.UserProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    val id: Long,
    val reviewId: Long,
    val userId: String? = null,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val likeCount: Int = 0,
    val userProfile: UserProfileDto? = null,
    val userHasLiked: Boolean = false,
    val userUid: String? = null,
    val userName: String? = null,
    val userProfileImageUrl: String? = null
) {
    fun toComment(): Comment {
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
        
        // Make sure likeCount is never negative
        val safeLikeCount = if (likeCount < 0) 0 else likeCount
        
        return Comment(
            id = id,
            reviewId = reviewId,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt,
            likeCount = safeLikeCount,
            userProfile = profile,
            userHasLiked = userHasLiked
        )
    }
}

@Serializable
data class CommentRequest(
    val reviewId: Long,
    val content: String
)

@Serializable
data class CommentResponse(
    val success: Boolean = true,
    val message: String? = null,
    val data: CommentDto? = null
)

@Serializable
data class CommentApiResponse(
    val success: Boolean = true,
    val message: String? = null,
    val data: CommentDto? = null
)

@Serializable
data class CommentsPageResponse(
    val content: List<CommentDto>,
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
