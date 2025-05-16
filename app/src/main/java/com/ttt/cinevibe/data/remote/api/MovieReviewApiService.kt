package com.ttt.cinevibe.data.remote.api

import com.ttt.cinevibe.data.remote.dto.BooleanResponse
import com.ttt.cinevibe.data.remote.dto.CreateReviewRequest
import com.ttt.cinevibe.data.remote.dto.MovieReviewDto
import com.ttt.cinevibe.data.remote.dto.MovieReviewResponse
import com.ttt.cinevibe.data.remote.dto.ReviewApiResponse
import com.ttt.cinevibe.data.remote.dto.UpdateReviewRequest
import retrofit2.http.*

interface MovieReviewApiService {
    
    // Get reviews created by the authenticated user
    @GET("api/reviews/my")
    suspend fun getUserReviews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): MovieReviewResponse
    
    // Get reviews for a specific user by their ID
    @GET("api/reviews/user/{userId}")
    suspend fun getUserReviewsByUserId(
        @Path("userId") userId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): MovieReviewResponse
    
    // Get reviews for a specific movie
    @GET("api/reviews/movie/{tmdbMovieId}")
    suspend fun getMovieReviews(
        @Path("tmdbMovieId") tmdbMovieId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): MovieReviewResponse
    
    // Get a specific review by ID
    @GET("api/reviews/{reviewId}")
    suspend fun getReviewById(
        @Path("reviewId") reviewId: Long
    ): ReviewApiResponse
    
    // Create a new review
    @POST("api/reviews")
    suspend fun createReview(
        @Body request: CreateReviewRequest
    ): ReviewApiResponse
    
    // Update an existing review
    @PUT("api/reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewId: Long,
        @Body request: UpdateReviewRequest
    ): ReviewApiResponse
    
    // Delete a review
    @DELETE("api/reviews/{reviewId}")
    suspend fun deleteReview(
        @Path("reviewId") reviewId: Long
    )
      // Like a review
    @POST("api/reviews/{reviewId}/like")
    suspend fun likeReview(
        @Path("reviewId") reviewId: Long
    ): ReviewApiResponse
    
    // Unlike a review
    @DELETE("api/reviews/{reviewId}/like")
    suspend fun unlikeReview(
        @Path("reviewId") reviewId: Long
    ): ReviewApiResponse
      // Check if user has reviewed a movie
    @GET("api/reviews/check/{tmdbMovieId}")
    suspend fun hasUserReviewedMovie(
        @Path("tmdbMovieId") tmdbMovieId: Long
    ): BooleanResponse
    
    // Get user's review for a movie
    @GET("api/reviews/user/movie/{tmdbMovieId}")
    suspend fun getUserReviewForMovie(
        @Path("tmdbMovieId") tmdbMovieId: Long
    ): ReviewApiResponse
    
    // Get reviews from users the authenticated user is following
    @GET("api/reviews/following")
    suspend fun getFollowingReviews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): MovieReviewResponse
    
    // Get popular reviews across all users
    @GET("api/reviews/popular")
    suspend fun getPopularReviews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): MovieReviewResponse
    
    // Get trending reviews (high engagement, recent)
    @GET("api/reviews/trending")
    suspend fun getTrendingReviews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): MovieReviewResponse
}