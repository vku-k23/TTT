package com.ttt.cinevibe.data.repository

import com.ttt.cinevibe.data.remote.api.CommentApiService
import com.ttt.cinevibe.data.remote.dto.CommentRequest
import com.ttt.cinevibe.domain.model.Comment
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.domain.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val commentApiService: CommentApiService
) : CommentRepository {

    override suspend fun getReviewComments(reviewId: Long, page: Int, size: Int): Flow<Resource<List<Comment>>> = flow {
        emit(Resource.Loading())
        try {
            val response = commentApiService.getReviewComments(reviewId, page, size)
            
            val comments = response.content.map { dto ->
                dto.toComment()
            }
            
            emit(Resource.Success(comments))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error fetching comments"))
        }
    }

    override suspend fun getCommentById(commentId: Long): Flow<Resource<Comment>> = flow {
        emit(Resource.Loading())
        try {
            val response = commentApiService.getCommentById(commentId)
            if (response.data != null) {
                emit(Resource.Success(response.data.toComment()))
            } else {
                emit(Resource.Error(response.message ?: "Failed to get comment"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    override suspend fun createComment(reviewId: Long, content: String): Flow<Resource<Comment>> = flow {
        emit(Resource.Loading())
        try {
            val request = CommentRequest(reviewId, content)
            val response = commentApiService.createComment(request)
            
            if (response.data != null) {
                emit(Resource.Success(response.data.toComment()))
            } else {
                emit(Resource.Error(response.message ?: "Failed to create comment"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    override suspend fun updateComment(commentId: Long, content: String): Flow<Resource<Comment>> = flow {
        emit(Resource.Loading())
        try {
            val request = CommentRequest(0, content) // reviewId is not used in update
            val response = commentApiService.updateComment(commentId, request)
            
            if (response.data != null) {
                emit(Resource.Success(response.data.toComment()))
            } else {
                emit(Resource.Error(response.message ?: "Failed to update comment"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    override suspend fun deleteComment(commentId: Long): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            commentApiService.deleteComment(commentId)
            emit(Resource.Success(Unit))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    override suspend fun likeComment(commentId: Long): Flow<Resource<Comment>> = flow {
        emit(Resource.Loading())
        try {
            val response = commentApiService.likeComment(commentId)
            
            if (response.data != null) {
                emit(Resource.Success(response.data.toComment()))
            } else {
                emit(Resource.Error(response.message ?: "Failed to like comment"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    override suspend fun unlikeComment(commentId: Long): Flow<Resource<Comment>> = flow {
        emit(Resource.Loading())
        try {
            val response = commentApiService.unlikeComment(commentId)
            
            if (response.data != null) {
                emit(Resource.Success(response.data.toComment()))
            } else {
                emit(Resource.Error(response.message ?: "Failed to unlike comment"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }
}
