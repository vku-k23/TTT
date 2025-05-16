package com.ttt.cinevibe.domain.repository

import com.ttt.cinevibe.domain.model.Comment
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    suspend fun getReviewComments(reviewId: Long, page: Int, size: Int): Flow<Resource<List<Comment>>>
    
    suspend fun getCommentById(commentId: Long): Flow<Resource<Comment>>
    
    suspend fun createComment(reviewId: Long, content: String): Flow<Resource<Comment>>
    
    suspend fun updateComment(commentId: Long, content: String): Flow<Resource<Comment>>
    
    suspend fun deleteComment(commentId: Long): Flow<Resource<Unit>>
    
    suspend fun likeComment(commentId: Long): Flow<Resource<Comment>>
    
    suspend fun unlikeComment(commentId: Long): Flow<Resource<Comment>>
}
