package com.ttt.cinevibe.data.remote.api

import com.ttt.cinevibe.data.remote.dto.CommentApiResponse
import com.ttt.cinevibe.data.remote.dto.CommentRequest
import com.ttt.cinevibe.data.remote.dto.CommentResponse
import com.ttt.cinevibe.data.remote.dto.CommentsPageResponse
import retrofit2.http.*

interface CommentApiService {
    // Get comments for a review
    @GET("api/comments/review/{reviewId}")
    suspend fun getReviewComments(
        @Path("reviewId") reviewId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): CommentsPageResponse
    
    // Get comment by ID
    @GET("api/comments/{commentId}")
    suspend fun getCommentById(
        @Path("commentId") commentId: Long
    ): CommentResponse
    
    // Create a new comment
    @POST("api/comments")
    suspend fun createComment(
        @Body request: CommentRequest
    ): CommentResponse
    
    // Update an existing comment
    @PUT("api/comments/{commentId}")
    suspend fun updateComment(
        @Path("commentId") commentId: Long,
        @Body request: CommentRequest
    ): CommentResponse
    
    // Delete a comment
    @DELETE("api/comments/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: Long
    )
    
    // Like a comment
    @POST("api/comments/{commentId}/like")
    suspend fun likeComment(
        @Path("commentId") commentId: Long
    ): CommentResponse
    
    // Unlike a comment
    @DELETE("api/comments/{commentId}/like")
    suspend fun unlikeComment(
        @Path("commentId") commentId: Long
    ): CommentResponse
}
