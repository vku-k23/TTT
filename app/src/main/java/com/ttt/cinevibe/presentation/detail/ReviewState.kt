package com.ttt.cinevibe.presentation.detail

// State classes for review operations in Movie Detail

sealed class ReviewOperationState {
    object Idle : ReviewOperationState()
    object Loading : ReviewOperationState()
    object Success : ReviewOperationState()
    data class Error(val message: String) : ReviewOperationState()
}

sealed class HasReviewedState {
    object Loading : HasReviewedState()
    data class Success(val hasReviewed: Boolean) : HasReviewedState()
    data class Error(val message: String) : HasReviewedState()
    object Reviewed : HasReviewedState()
    object NotReviewed : HasReviewedState()
}
