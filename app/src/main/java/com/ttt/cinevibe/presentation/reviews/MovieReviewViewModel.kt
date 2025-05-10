package com.ttt.cinevibe.presentation.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.domain.model.MovieReview
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.domain.repository.MovieReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieReviewViewModel @Inject constructor(
    private val reviewRepository: MovieReviewRepository
) : ViewModel() {

    // State for movie reviews
    private val _movieReviewsState = MutableStateFlow<MovieReviewsState>(MovieReviewsState.Initial)
    val movieReviewsState = _movieReviewsState.asStateFlow()
    
    // State for user reviews
    private val _userReviewsState = MutableStateFlow<MovieReviewsState>(MovieReviewsState.Initial)
    val userReviewsState = _userReviewsState.asStateFlow()
    
    // State for review operations (create, update, delete)
    private val _reviewOperationState = MutableStateFlow<ReviewOperationState>(ReviewOperationState.Initial)
    val reviewOperationState = _reviewOperationState.asStateFlow()
    
    // State to track if user has reviewed a movie
    private val _hasReviewedState = MutableStateFlow<HasReviewedState>(HasReviewedState.Initial)
    val hasReviewedState = _hasReviewedState.asStateFlow()
    
    // Current page and size for pagination
    private var currentMovieReviewPage = 0
    private var currentUserReviewPage = 0
    private val pageSize = 10
    
    // Function to load reviews for a specific movie
    fun getMovieReviews(tmdbMovieId: Long, refresh: Boolean = false) {
        if (refresh) {
            currentMovieReviewPage = 0
            _movieReviewsState.value = MovieReviewsState.Initial
        }
        
        viewModelScope.launch {
            reviewRepository.getMovieReviews(tmdbMovieId, currentMovieReviewPage, pageSize)
                .collectLatest { result ->
                    when (result) {
                        is Resource.Loading -> {
                            if (currentMovieReviewPage == 0) {
                                _movieReviewsState.value = MovieReviewsState.Loading
                            } else {
                                _movieReviewsState.value = MovieReviewsState.LoadingMore(
                                    (_movieReviewsState.value as? MovieReviewsState.Success)?.reviews ?: emptyList()
                                )
                            }
                        }
                        is Resource.Success -> {
                            val currentReviews = if (currentMovieReviewPage > 0 && _movieReviewsState.value is MovieReviewsState.Success) {
                                (_movieReviewsState.value as MovieReviewsState.Success).reviews
                            } else {
                                emptyList()
                            }
                            
                            val updatedReviews = if (currentMovieReviewPage == 0) {
                                result.data ?: emptyList()
                            } else {
                                currentReviews + (result.data ?: emptyList())
                            }
                            
                            _movieReviewsState.value = MovieReviewsState.Success(updatedReviews)
                            
                            // Increment page for next load
                            if ((result.data?.size ?: 0) >= pageSize) {
                                currentMovieReviewPage++
                            }
                        }
                        is Resource.Error -> {
                            _movieReviewsState.value = MovieReviewsState.Error(result.message ?: "Unknown error")
                        }
                    }
                }
        }
    }
    
    // Function to load user's own reviews
    fun getUserReviews(refresh: Boolean = false) {
        if (refresh) {
            currentUserReviewPage = 0
            _userReviewsState.value = MovieReviewsState.Initial
        }
        
        viewModelScope.launch {
            reviewRepository.getUserReviews(currentUserReviewPage, pageSize)
                .collectLatest { result ->
                    when (result) {
                        is Resource.Loading -> {
                            if (currentUserReviewPage == 0) {
                                _userReviewsState.value = MovieReviewsState.Loading
                            } else {
                                _userReviewsState.value = MovieReviewsState.LoadingMore(
                                    (_userReviewsState.value as? MovieReviewsState.Success)?.reviews ?: emptyList()
                                )
                            }
                        }
                        is Resource.Success -> {
                            val currentReviews = if (currentUserReviewPage > 0 && _userReviewsState.value is MovieReviewsState.Success) {
                                (_userReviewsState.value as MovieReviewsState.Success).reviews
                            } else {
                                emptyList()
                            }
                            
                            val updatedReviews = if (currentUserReviewPage == 0) {
                                result.data ?: emptyList()
                            } else {
                                currentReviews + (result.data ?: emptyList())
                            }
                            
                            _userReviewsState.value = MovieReviewsState.Success(updatedReviews)
                            
                            // Increment page for next load
                            if ((result.data?.size ?: 0) >= pageSize) {
                                currentUserReviewPage++
                            }
                        }
                        is Resource.Error -> {
                            _userReviewsState.value = MovieReviewsState.Error(result.message ?: "Unknown error")
                        }
                    }
                }
        }
    }
    
    // Function to create a new review
    fun createReview(tmdbMovieId: Long, rating: Int, content: String) {
        viewModelScope.launch {
            _reviewOperationState.value = ReviewOperationState.Loading
            reviewRepository.createReview(tmdbMovieId, rating, content)
                .collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            _reviewOperationState.value = ReviewOperationState.Success(
                                result.data,
                                OperationType.CREATE
                            )
                            // Reset after successful operation
                            resetOperationStateAfterDelay()
                        }
                        is Resource.Error -> {
                            _reviewOperationState.value = ReviewOperationState.Error(
                                result.message ?: "Failed to create review",
                                OperationType.CREATE
                            )
                        }
                        is Resource.Loading -> {
                            // Already handled above
                        }
                    }
                }
        }
    }
    
    // Function to update an existing review
    fun updateReview(reviewId: Long, rating: Int, content: String) {
        viewModelScope.launch {
            _reviewOperationState.value = ReviewOperationState.Loading
            reviewRepository.updateReview(reviewId, rating, content)
                .collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            _reviewOperationState.value = ReviewOperationState.Success(
                                result.data,
                                OperationType.UPDATE
                            )
                            // Reset after successful operation
                            resetOperationStateAfterDelay()
                        }
                        is Resource.Error -> {
                            _reviewOperationState.value = ReviewOperationState.Error(
                                result.message ?: "Failed to update review",
                                OperationType.UPDATE
                            )
                        }
                        is Resource.Loading -> {
                            // Already handled above
                        }
                    }
                }
        }
    }
    
    // Function to delete a review
    fun deleteReview(reviewId: Long) {
        viewModelScope.launch {
            _reviewOperationState.value = ReviewOperationState.Loading
            reviewRepository.deleteReview(reviewId)
                .collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            _reviewOperationState.value = ReviewOperationState.Success(
                                null,
                                OperationType.DELETE
                            )
                            // Reset after successful operation
                            resetOperationStateAfterDelay()
                        }
                        is Resource.Error -> {
                            _reviewOperationState.value = ReviewOperationState.Error(
                                result.message ?: "Failed to delete review",
                                OperationType.DELETE
                            )
                        }
                        is Resource.Loading -> {
                            // Already handled above
                        }
                    }
                }
        }
    }
    
    // Function to like a review
    fun likeReview(reviewId: Long) {
        viewModelScope.launch {
            reviewRepository.likeReview(reviewId)
                .collectLatest { result ->
                    // We don't update the state here as we'll refresh the entire list after operation
                }
        }
    }
    
    // Function to unlike a review
    fun unlikeReview(reviewId: Long) {
        viewModelScope.launch {
            reviewRepository.unlikeReview(reviewId)
                .collectLatest { result ->
                    // We don't update the state here as we'll refresh the entire list after operation
                }
        }
    }
    
    // Function to check if user has already reviewed a movie
    fun checkIfUserReviewedMovie(tmdbMovieId: Long) {
        viewModelScope.launch {
            _hasReviewedState.value = HasReviewedState.Loading
            reviewRepository.hasUserReviewedMovie(tmdbMovieId)
                .collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            _hasReviewedState.value = HasReviewedState.Success(result.data ?: false)
                        }
                        is Resource.Error -> {
                            _hasReviewedState.value = HasReviewedState.Error(result.message ?: "Failed to check review status")
                        }
                        is Resource.Loading -> {
                            // Already handled above
                        }
                    }
                }
        }
    }
    
    // Helper function to reset operation state after delay
    private fun resetOperationStateAfterDelay() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _reviewOperationState.value = ReviewOperationState.Initial
        }
    }
}

// State classes for MovieReviewViewModel
sealed class MovieReviewsState {
    object Initial : MovieReviewsState()
    object Loading : MovieReviewsState()
    data class LoadingMore(val currentReviews: List<MovieReview>) : MovieReviewsState()
    data class Success(val reviews: List<MovieReview>) : MovieReviewsState()
    data class Error(val message: String) : MovieReviewsState()
}

sealed class ReviewOperationState {
    object Initial : ReviewOperationState()
    object Loading : ReviewOperationState()
    data class Success(val review: MovieReview?, val type: OperationType) : ReviewOperationState()
    data class Error(val message: String, val type: OperationType) : ReviewOperationState()
}

sealed class HasReviewedState {
    object Initial : HasReviewedState()
    object Loading : HasReviewedState()
    data class Success(val hasReviewed: Boolean) : HasReviewedState()
    data class Error(val message: String) : HasReviewedState()
}

enum class OperationType {
    CREATE, UPDATE, DELETE, LIKE, UNLIKE
}