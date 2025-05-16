package com.ttt.cinevibe.presentation.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ttt.cinevibe.domain.model.Comment
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.domain.repository.CommentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _commentsState = MutableStateFlow<CommentsState>(CommentsState.Loading)
    val commentsState: StateFlow<CommentsState> = _commentsState.asStateFlow()
    
    private val _commentOperationState = MutableStateFlow<CommentOperationState>(CommentOperationState.Idle)
    val commentOperationState: StateFlow<CommentOperationState> = _commentOperationState.asStateFlow()
    
    private var currentPage = 0
    private var isLastPage = false
    private var reviewId: Long = 0
    
    fun loadComments(reviewId: Long, refresh: Boolean = false) {
        if (refresh) {
            currentPage = 0
            isLastPage = false
            this.reviewId = reviewId
        }
        
        if (isLastPage && !refresh) return
        
        viewModelScope.launch {
            _commentsState.update { 
                if (currentPage == 0) CommentsState.Loading else it 
            }
            
            commentRepository.getReviewComments(reviewId, currentPage, 20).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val newComments = result.data ?: emptyList()
                        isLastPage = newComments.isEmpty()
                        
                        _commentsState.update { currentState ->
                            when (currentState) {
                                is CommentsState.Loading -> CommentsState.Success(newComments)
                                is CommentsState.Success -> {
                                    if (refresh) {
                                        CommentsState.Success(newComments)
                                    } else {
                                        CommentsState.Success(currentState.comments + newComments)
                                    }
                                }
                                is CommentsState.Error -> CommentsState.Success(newComments)
                            }
                        }
                        
                        if (!isLastPage) currentPage++
                    }
                    is Resource.Error -> {
                        _commentsState.update { CommentsState.Error(result.message ?: "Failed to load comments") }
                    }
                    is Resource.Loading -> {
                        if (currentPage == 0) {
                            _commentsState.update { CommentsState.Loading }
                        }
                    }
                }
            }
        }
    }
    
    fun createComment(reviewId: Long, content: String) {
        viewModelScope.launch {
            _commentOperationState.value = CommentOperationState.Loading
            
            commentRepository.createComment(reviewId, content).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _commentOperationState.value = CommentOperationState.Success("Comment added successfully")
                        loadComments(reviewId, true)
                    }
                    is Resource.Error -> {
                        _commentOperationState.value = CommentOperationState.Error(result.message ?: "Failed to add comment")
                    }
                    is Resource.Loading -> {
                        _commentOperationState.value = CommentOperationState.Loading
                    }
                }
            }
        }
    }
    
    fun updateComment(commentId: Long, content: String) {
        viewModelScope.launch {
            _commentOperationState.value = CommentOperationState.Loading
            
            commentRepository.updateComment(commentId, content).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _commentOperationState.value = CommentOperationState.Success("Comment updated successfully")
                        loadComments(reviewId, true)
                    }
                    is Resource.Error -> {
                        _commentOperationState.value = CommentOperationState.Error(result.message ?: "Failed to update comment")
                    }
                    is Resource.Loading -> {
                        _commentOperationState.value = CommentOperationState.Loading
                    }
                }
            }
        }
    }
    
    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            _commentOperationState.value = CommentOperationState.Loading
            
            commentRepository.deleteComment(commentId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _commentOperationState.value = CommentOperationState.Success("Comment deleted successfully")
                        loadComments(reviewId, true)
                    }
                    is Resource.Error -> {
                        _commentOperationState.value = CommentOperationState.Error(result.message ?: "Failed to delete comment")
                    }
                    is Resource.Loading -> {
                        _commentOperationState.value = CommentOperationState.Loading
                    }
                }
            }
        }
    }
    
    fun likeComment(commentId: Long) {
        viewModelScope.launch {
            commentRepository.likeComment(commentId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        // Update the comment in the state
                        updateCommentInState(result.data)
                    }
                    is Resource.Error -> {
                        _commentOperationState.value = CommentOperationState.Error(result.message ?: "Failed to like comment")
                    }
                    else -> {} // Do nothing for loading
                }
            }
        }
    }
    
    fun unlikeComment(commentId: Long) {
        viewModelScope.launch {
            commentRepository.unlikeComment(commentId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        // Update the comment in the state
                        updateCommentInState(result.data)
                    }
                    is Resource.Error -> {
                        _commentOperationState.value = CommentOperationState.Error(result.message ?: "Failed to unlike comment")
                    }
                    else -> {} // Do nothing for loading
                }
            }
        }
    }
    
    private fun updateCommentInState(updatedComment: Comment?) {
        if (updatedComment == null) return
        
        _commentsState.update { currentState ->
            if (currentState is CommentsState.Success) {
                val updatedComments = currentState.comments.map { comment ->
                    if (comment.id == updatedComment.id) updatedComment else comment
                }
                CommentsState.Success(updatedComments)
            } else {
                currentState
            }
        }
    }
    
    fun resetOperationState() {
        _commentOperationState.value = CommentOperationState.Idle
    }
    
    fun isCurrentUserAuthor(comment: Comment): Boolean {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        return currentUserUid == comment.userProfile.uid
    }
}

sealed class CommentsState {
    object Loading : CommentsState()
    data class Success(val comments: List<Comment>) : CommentsState()
    data class Error(val message: String) : CommentsState()
}

sealed class CommentOperationState {
    object Idle : CommentOperationState()
    object Loading : CommentOperationState()
    data class Success(val message: String) : CommentOperationState()
    data class Error(val message: String) : CommentOperationState()
}
