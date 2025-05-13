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
            
            // Get the current state of the review before optimistic update
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
                    .collect { result -> // Use collect instead of collectLatest
                        when (result) {
                            is Resource.Success -> {
                                android.util.Log.d("MovieReviewVM", "Like API response for review $reviewId: likeCount=${result.data?.likeCount}")
                                // Update the review in the state with the updated data from server
                                result.data?.let { 
                                    // Keep userHasLiked as true regardless of what came from server
                                    val updatedReview = it.copy(userHasLiked = true)
                                    updateReviewInState(updatedReview)
                                }
                            }
                            is Resource.Error -> {
                                android.util.Log.e("MovieReviewVM", "Error liking review: ${result.message}")
                                // Revert the optimistic update if there was an error
                                updateReviewLikeState(reviewId, false)
                            }
                            is Resource.Loading -> {
                                // Already handled via optimistic update
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
                    .collect { result -> // Use collect instead of collectLatest
                        when (result) {
                            is Resource.Success -> {
                                android.util.Log.d("MovieReviewVM", "Unlike API response for review $reviewId: likeCount=${result.data?.likeCount}")
                                // Update the review in the state with the updated data
                                result.data?.let { 
                                    // Keep userHasLiked as false regardless of what came from server
                                    val updatedReview = it.copy(userHasLiked = false)
                                    updateReviewInState(updatedReview)
                                }
                            }
                            is Resource.Error -> {
                                android.util.Log.e("MovieReviewVM", "Error unliking review: ${result.message}")
                                // Revert the optimistic update if there was an error
                                updateReviewLikeState(reviewId, true)
                            }
                            is Resource.Loading -> {
                                // Already handled via optimistic update
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
    
    // Helper function to update a review's like state in place for both movie and user reviews states
    private fun updateReviewLikeState(reviewId: Long, liked: Boolean) {
        // Update in movie reviews state
        updateReviewLikeStateInList(reviewId, liked, _movieReviewsState) { updatedState ->
            _movieReviewsState.value = updatedState
        }
        
        // Also update in user reviews state if present
        updateReviewLikeStateInList(reviewId, liked, _userReviewsState) { updatedState ->
            _userReviewsState.value = updatedState
        }
    }
    
    // Helper function to update like state in a specific review list
    private fun updateReviewLikeStateInList(
        reviewId: Long, 
        liked: Boolean, 
        stateFlow: MutableStateFlow<MovieReviewsState>, 
        updateState: (MovieReviewsState) -> Unit
    ) {
        if (stateFlow.value is MovieReviewsState.Success) {
            val reviews = (stateFlow.value as MovieReviewsState.Success).reviews.toMutableList()
            val index = reviews.indexOfFirst { it.id == reviewId }
            
            if (index != -1) {
                val review = reviews[index]
                val oldLikeCount = review.likeCount
                val newLikeCount = if (liked) oldLikeCount + 1 else (oldLikeCount - 1).coerceAtLeast(0)
                
                // Log the change
                android.util.Log.d("MovieReviewVM", "Optimistically updating review $reviewId like state: " +
                        "oldLikeCount=$oldLikeCount, newLikeCount=$newLikeCount, " +
                        "oldUserHasLiked=${review.userHasLiked}, newUserHasLiked=$liked")
                
                // Create updated review with new values
                val updatedReview = review.copy(
                    userHasLiked = liked,
                    likeCount = newLikeCount
                )
                
                // Update the list
                reviews[index] = updatedReview
                updateState(MovieReviewsState.Success(reviews))
                
                android.util.Log.d("MovieReviewVM", "Updated review $reviewId in list successfully")
            } else {
                android.util.Log.d("MovieReviewVM", "Review $reviewId not found in this list")
            }
        }
    }
    
    // Helper function to update a review in the state
    private fun updateReviewInState(updatedReview: MovieReview?) {
        if (updatedReview == null) return
        
        // Update in movie reviews state
        updateReviewInSpecificState(updatedReview, _movieReviewsState) { updatedState ->
            _movieReviewsState.value = updatedState
        }
        
        // Also update in user reviews state if present
        updateReviewInSpecificState(updatedReview, _userReviewsState) { updatedState ->
            _userReviewsState.value = updatedState
        }
    }
    
    // Helper function to update a review in a specific state
    private fun updateReviewInSpecificState(
        updatedReview: MovieReview,
        stateFlow: MutableStateFlow<MovieReviewsState>,
        updateState: (MovieReviewsState) -> Unit
    ) {
        if (stateFlow.value is MovieReviewsState.Success) {
            val reviews = (stateFlow.value as MovieReviewsState.Success).reviews.toMutableList()
            val index = reviews.indexOfFirst { it.id == updatedReview.id }
            
            if (index != -1) {
                // Get current review state in the UI
                val currentReview = reviews[index]
                
                // Log detailed information about the update
                android.util.Log.d("MovieReviewVM", 
                    "Updating review ${updatedReview.id} in state:\n" +
                    "Before: likeCount=${currentReview.likeCount}, userHasLiked=${currentReview.userHasLiked}\n" +
                    "After: likeCount=${updatedReview.likeCount}, userHasLiked=${updatedReview.userHasLiked}")
                
                // Create a new review object with updated likeCount
                // Always use the server likeCount value but keep UI state for userHasLiked
                val finalReview = currentReview.copy(
                    likeCount = updatedReview.likeCount,
                    // Preserve the current userHasLiked state from the UI
                    userHasLiked = currentReview.userHasLiked  
                )
                
                reviews[index] = finalReview
                updateState(MovieReviewsState.Success(reviews))
                
                // Log the final update
                android.util.Log.d("MovieReviewVM", 
                    "Review ${updatedReview.id} updated with likeCount=${finalReview.likeCount}, userHasLiked=${finalReview.userHasLiked}")
            } else {
                android.util.Log.d("MovieReviewVM", "Review ${updatedReview.id} not found in this list")
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