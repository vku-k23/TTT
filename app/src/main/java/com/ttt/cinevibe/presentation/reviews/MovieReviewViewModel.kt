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
    
    // Track if we've reached the end of the list
    private var hasMoreMovieReviews = true
    private var hasMoreUserReviews = true
    
    // Track if we're already loading
    private var isLoadingMovieReviews = false
    private var isLoadingUserReviews = false
    
    // Function to load reviews for a specific movie
    fun getMovieReviews(tmdbMovieId: Long, refresh: Boolean = false) {
        // Don't load more if we're already loading or if we've reached the end
        if (isLoadingMovieReviews && !refresh) {
            return
        }
        
        if (!hasMoreMovieReviews && !refresh) {
            return
        }
        
        if (refresh) {
            currentMovieReviewPage = 0
            hasMoreMovieReviews = true
            _movieReviewsState.value = MovieReviewsState.Initial
        }
        
        isLoadingMovieReviews = true
        
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
                            isLoadingMovieReviews = false
                            
                            val newReviews = result.data ?: emptyList()
                            
                            // Check if we've reached the end
                            if (newReviews.size < pageSize) {
                                hasMoreMovieReviews = false
                            }
                            
                            val currentReviews = if (currentMovieReviewPage > 0 && _movieReviewsState.value is MovieReviewsState.Success) {
                                (_movieReviewsState.value as MovieReviewsState.Success).reviews
                            } else {
                                emptyList()
                            }
                            
                            val updatedReviews = if (currentMovieReviewPage == 0) {
                                newReviews
                            } else {
                                currentReviews + newReviews
                            }
                            
                            _movieReviewsState.value = MovieReviewsState.Success(updatedReviews)
                            
                            // Increment page for next load only if we have more to load
                            if (newReviews.size >= pageSize) {
                                currentMovieReviewPage++
                            }
                        }
                        is Resource.Error -> {
                            isLoadingMovieReviews = false
                            _movieReviewsState.value = MovieReviewsState.Error(result.message ?: "Unknown error")
                        }
                    }
                }
        }
    }
    
    // Function to load user's own reviews
    fun getUserReviews(refresh: Boolean = false) {
        // Don't load more if we're already loading or if we've reached the end
        if (isLoadingUserReviews && !refresh) {
            return
        }
        
        if (!hasMoreUserReviews && !refresh) {
            return
        }
        
        if (refresh) {
            currentUserReviewPage = 0
            hasMoreUserReviews = true
            _userReviewsState.value = MovieReviewsState.Initial
        }
        
        isLoadingUserReviews = true
        
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
                            isLoadingUserReviews = false
                            
                            val newReviews = result.data ?: emptyList()
                            
                            // Check if we've reached the end
                            if (newReviews.size < pageSize) {
                                hasMoreUserReviews = false
                            }
                            
                            val currentReviews = if (currentUserReviewPage > 0 && _userReviewsState.value is MovieReviewsState.Success) {
                                (_userReviewsState.value as MovieReviewsState.Success).reviews
                            } else {
                                emptyList()
                            }
                            
                            val updatedReviews = if (currentUserReviewPage == 0) {
                                newReviews
                            } else {
                                currentReviews + newReviews
                            }
                            
                            _userReviewsState.value = MovieReviewsState.Success(updatedReviews)
                            
                            // Increment page for next load only if we have more to load
                            if (newReviews.size >= pageSize) {
                                currentUserReviewPage++
                            }
                        }
                        is Resource.Error -> {
                            isLoadingUserReviews = false
                            _userReviewsState.value = MovieReviewsState.Error(result.message ?: "Unknown error")
                        }
                    }
                }
        }
    }
    
    // Function to create a new review
    fun createReview(tmdbMovieId: Long, rating: Float, content: String, movieTitle: String, containsSpoilers: Boolean = false) {
        viewModelScope.launch {
            _reviewOperationState.value = ReviewOperationState.Loading
            reviewRepository.createReview(tmdbMovieId, rating, content, movieTitle, containsSpoilers)
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
    fun updateReview(reviewId: Long, rating: Float, content: String, containsSpoilers: Boolean = false) {
        viewModelScope.launch {
            _reviewOperationState.value = ReviewOperationState.Loading
            reviewRepository.updateReview(reviewId, rating, content, containsSpoilers)
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
            android.util.Log.d("MovieReviewVM", "Liking review $reviewId")
            
            // Get the current state of the review before optimistic update for logging
            if (_movieReviewsState.value is MovieReviewsState.Success) {
                val reviews = (_movieReviewsState.value as MovieReviewsState.Success).reviews
                val review = reviews.find { it.id == reviewId }
                if (review != null) {
                    android.util.Log.d("MovieReviewVM", "Before like - Review $reviewId: likeCount=${review.likeCount}, userHasLiked=${review.userHasLiked}")
                }
            }
            
            // Optimistically update the UI
            updateReviewLikeState(reviewId, true)
            
            try {
                reviewRepository.likeReview(reviewId)
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                // Log the response data
                                android.util.Log.d("MovieReviewVM", "Like API response for review $reviewId: " +
                                        "likeCount=${result.data?.likeCount}, userHasLiked=${result.data?.userHasLiked}")
                                
                                // Update the review in the state with the accurate data from server
                                result.data?.let { updateReviewInState(it) }
                            }
                            is Resource.Error -> {
                                android.util.Log.e("MovieReviewVM", "Error liking review $reviewId: ${result.message}")
                                // Revert the optimistic update if there was an error
                                updateReviewLikeState(reviewId, false)
                            }
                            is Resource.Loading -> {
                                // Do nothing during loading state
                            }
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.e("MovieReviewVM", "Exception during likeReview: ${e.message}", e)
                // Revert the optimistic update if there was an exception
                updateReviewLikeState(reviewId, false)
            }
        }
    }
    
    // Function to unlike a review
    fun unlikeReview(reviewId: Long) {
        viewModelScope.launch {
            android.util.Log.d("MovieReviewVM", "Unliking review $reviewId")
            
            // Get the current state of the review before optimistic update
            if (_movieReviewsState.value is MovieReviewsState.Success) {
                val reviews = (_movieReviewsState.value as MovieReviewsState.Success).reviews
                val review = reviews.find { it.id == reviewId }
                if (review != null) {
                    android.util.Log.d("MovieReviewVM", "Before unlike - Review $reviewId: likeCount=${review.likeCount}, userHasLiked=${review.userHasLiked}")
                }
            }
            
            // Optimistically update the UI
            updateReviewLikeState(reviewId, false)
            
            try {
                reviewRepository.unlikeReview(reviewId)
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                // Log the response data
                                android.util.Log.d("MovieReviewVM", "Unlike API response for review $reviewId: " +
                                        "likeCount=${result.data?.likeCount}, userHasLiked=${result.data?.userHasLiked}")
                                
                                // Update the review in the state with the accurate data from server
                                result.data?.let { updateReviewInState(it) }
                            }
                            is Resource.Error -> {
                                android.util.Log.e("MovieReviewVM", "Error unliking review $reviewId: ${result.message}")
                                // Revert the optimistic update if there was an error
                                updateReviewLikeState(reviewId, true)
                            }
                            is Resource.Loading -> {
                                // Do nothing during loading state
                            }
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.e("MovieReviewVM", "Exception during unlikeReview: ${e.message}", e)
                // Revert the optimistic update if there was an exception
                updateReviewLikeState(reviewId, true)
            }
        }
    }
    
    // Helper function to update a review's like state in place
    private fun updateReviewLikeState(reviewId: Long, liked: Boolean) {
        if (_movieReviewsState.value is MovieReviewsState.Success) {
            val reviews = (_movieReviewsState.value as MovieReviewsState.Success).reviews.toMutableList()
            val index = reviews.indexOfFirst { it.id == reviewId }
            
            if (index != -1) {
                val review = reviews[index]
                val oldLikeCount = review.likeCount
                val newLikeCount = if (liked) oldLikeCount + 1 else (oldLikeCount - 1).coerceAtLeast(0)
                
                // Log the change for debugging
                android.util.Log.d("MovieReviewVM", "Optimistically updating review $reviewId like state: " +
                        "oldLikeCount=$oldLikeCount, newLikeCount=$newLikeCount, " +
                        "oldUserHasLiked=${review.userHasLiked}, newUserHasLiked=$liked")
                
                val updatedReview = review.copy(
                    userHasLiked = liked,
                    likeCount = newLikeCount
                )
                reviews[index] = updatedReview
                _movieReviewsState.value = MovieReviewsState.Success(reviews)
            } else {
                android.util.Log.w("MovieReviewVM", "Could not find review $reviewId to update like state")
            }
        } else {
            android.util.Log.w("MovieReviewVM", "Cannot update review like state - reviews not in Success state")
        }
    }
    
    // Helper function to update a review in the state
    private fun updateReviewInState(updatedReview: MovieReview?) {
        if (updatedReview != null && _movieReviewsState.value is MovieReviewsState.Success) {
            val reviews = (_movieReviewsState.value as MovieReviewsState.Success).reviews.toMutableList()
            val index = reviews.indexOfFirst { it.id == updatedReview.id }
            
            if (index != -1) {
                // Log before update for debugging
                android.util.Log.d("MovieReviewVM", "Updating review ${updatedReview.id}: " +
                        "Current likeCount=${reviews[index].likeCount}, " +
                        "New likeCount=${updatedReview.likeCount}, " +
                        "userHasLiked=${updatedReview.userHasLiked}")
                
                // Ensure we're using the updated likeCount from the API response
                reviews[index] = updatedReview.copy(
                    likeCount = updatedReview.likeCount
                )
                
                _movieReviewsState.value = MovieReviewsState.Success(reviews)
                
                // Log after update
                android.util.Log.d("MovieReviewVM", "Updated review ${updatedReview.id} with likeCount=${updatedReview.likeCount}")
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