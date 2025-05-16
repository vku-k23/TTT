package com.ttt.cinevibe.domain.repository

import com.ttt.cinevibe.domain.model.MovieReview
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface MovieReviewRepository {
    suspend fun getUserReviews(page: Int, size: Int): Flow<Resource<List<MovieReview>>>
    
    suspend fun getMovieReviews(tmdbMovieId: Long, page: Int, size: Int): Flow<Resource<List<MovieReview>>>
    
    suspend fun getReviewById(reviewId: Long): Flow<Resource<MovieReview>>
    
    suspend fun createReview(tmdbMovieId: Long, rating: Float, content: String, movieTitle: String, containsSpoilers: Boolean): Flow<Resource<MovieReview>>
    
    suspend fun updateReview(reviewId: Long, rating: Float, content: String, containsSpoilers: Boolean, tmdbMovieId: Long? = null, movieTitle: String? = null): Flow<Resource<MovieReview>>
    
    suspend fun deleteReview(reviewId: Long): Flow<Resource<Unit>>
    
    suspend fun likeReview(reviewId: Long): Flow<Resource<MovieReview>>
    
    suspend fun unlikeReview(reviewId: Long): Flow<Resource<MovieReview>>
    
    suspend fun hasUserReviewedMovie(tmdbMovieId: Long): Flow<Resource<Boolean>>
    
    suspend fun getUserReviewForMovie(tmdbMovieId: Long): Flow<Resource<MovieReview>>
    
    // Get reviews by user ID
    suspend fun getUserReviewsByUserId(userId: String, page: Int, size: Int): Flow<Resource<List<MovieReview>>>
    
    // New methods for feed functionality
    suspend fun getFollowingReviews(page: Int, size: Int): Flow<Resource<List<MovieReview>>>
    
    suspend fun getPopularReviews(page: Int, size: Int): Flow<Resource<List<MovieReview>>>
    
    suspend fun getTrendingReviews(page: Int, size: Int): Flow<Resource<List<MovieReview>>>
}