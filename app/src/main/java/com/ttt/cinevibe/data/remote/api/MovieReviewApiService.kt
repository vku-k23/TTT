package com.ttt.cinevibe.data.remote.api

import com.ttt.cinevibe.data.remote.models.HasReviewedResponse
import com.ttt.cinevibe.data.remote.models.MovieReviewRequest
import com.ttt.cinevibe.data.remote.models.MovieReviewResponse
import com.ttt.cinevibe.data.remote.models.PagedMovieReviewsResponse
import retrofit2.http.*

interface MovieReviewApiService {
    
    // Get reviews created by the authenticated user
    @GET("api/reviews/my")
    suspend fun getUserReviews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PagedMovieReviewsResponse
    
    // Get reviews for a specific movie
    @GET("api/reviews/movie/{tmdbMovieId}")
    suspend fun getMovieReviews(
        @Path("tmdbMovieId") tmdbMovieId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PagedMovieReviewsResponse
    
    // Get a specific review by ID
    @GET("api/reviews/{reviewId}")
    suspend fun getReviewById(
        @Path("reviewId") reviewId: Long
    ): MovieReviewResponse
    
    // Create a new review
    @POST("api/reviews")
    suspend fun createReview(
        @Body request: MovieReviewRequest
    ): MovieReviewResponse
    
    // Update an existing review
    @PUT("api/reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewId: Long,
        @Body request: MovieReviewRequest
    ): MovieReviewResponse
    
    // Delete a review
    @DELETE("api/reviews/{reviewId}")
    suspend fun deleteReview(
        @Path("reviewId") reviewId: Long
    )
    
    // Like a review
    @POST("api/reviews/{reviewId}/like")
    suspend fun likeReview(
        @Path("reviewId") reviewId: Long
    ): MovieReviewResponse
    
    // Unlike a review
    @DELETE("api/reviews/{reviewId}/like")
    suspend fun unlikeReview(
        @Path("reviewId") reviewId: Long
    ): MovieReviewResponse
    
    // Check if user has reviewed a movie
    @GET("api/reviews/check/{tmdbMovieId}")
    suspend fun hasUserReviewedMovie(
        @Path("tmdbMovieId") tmdbMovieId: Long
    ): HasReviewedResponse
}