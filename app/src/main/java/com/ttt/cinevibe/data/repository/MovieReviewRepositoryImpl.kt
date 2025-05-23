package com.ttt.cinevibe.data.repository

import com.ttt.cinevibe.data.remote.api.MovieReviewApiService
import com.ttt.cinevibe.data.remote.dto.CreateReviewRequest
import com.ttt.cinevibe.data.remote.dto.UpdateReviewRequest
import com.ttt.cinevibe.domain.model.MovieReview
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.domain.model.UserProfile
import com.ttt.cinevibe.domain.repository.MovieReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class MovieReviewRepositoryImpl @Inject constructor(
    private val movieReviewApiService: MovieReviewApiService
) : MovieReviewRepository {

    override suspend fun getUserReviews(page: Int, size: Int): Flow<Resource<List<MovieReview>>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieReviewApiService.getUserReviews(page, size)
            
            // Chuyển đổi dữ liệu DTO với log để debug
            val reviews = response.content.map { dto ->
                android.util.Log.d("MovieReviewRepo", "User Review ${dto.id} - Content: ${dto.content}, ReviewText: ${dto.reviewText}")
                dto.toMovieReview()
            }
            
            emit(Resource.Success(reviews))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error fetching user reviews"))
        }
    }

    override suspend fun getUserReviewsByUserId(userId: String, page: Int, size: Int): Flow<Resource<List<MovieReview>>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieReviewApiService.getUserReviewsByUserId(userId, page, size)
            
            // Convert DTOs to domain models
            val reviews = response.content.map { dto ->
                android.util.Log.d("MovieReviewRepo", "User ${userId} Review ${dto.id} - Content: ${dto.content}, ReviewText: ${dto.reviewText}")
                dto.toMovieReview()
            }
            
            emit(Resource.Success(reviews))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error fetching reviews for user $userId"))
        }
    }

    override suspend fun getMovieReviews(tmdbMovieId: Long, page: Int, size: Int): Flow<Resource<List<MovieReview>>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieReviewApiService.getMovieReviews(tmdbMovieId, page, size)
            
            // Chuyển đổi dữ liệu DTO với kiểm tra chất lượng
            val reviews = response.content.map { dto ->
                // Log để debug
                android.util.Log.d("MovieReviewRepo", "Review ${dto.id} - User: ${dto.userProfile?.displayName ?: dto.userName ?: "No name"}")
                android.util.Log.d("MovieReviewRepo", "Review ${dto.id} - Content: ${dto.content}, ReviewText: ${dto.reviewText}")
                dto.toMovieReview()
            }
            
            emit(Resource.Success(reviews))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error fetching movie reviews"))
        }
    }

    override suspend fun getReviewById(reviewId: Long): Flow<Resource<MovieReview>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieReviewApiService.getReviewById(reviewId)
            if (response.success && response.data != null) {
                emit(Resource.Success(response.data.toMovieReview()))
            } else {
                emit(Resource.Error(response.message ?: "Failed to get review"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    override suspend fun createReview(tmdbMovieId: Long, rating: Float, content: String, movieTitle: String, containsSpoilers: Boolean): Flow<Resource<MovieReview>> = flow {
        emit(Resource.Loading())
        try {
            val safeContent = content.ifBlank { "" }
            val request = CreateReviewRequest(
                tmdbMovieId = tmdbMovieId, 
                movieTitle = movieTitle, 
                reviewText = safeContent,
                rating = rating,
                containsSpoilers = containsSpoilers
            )
            
            android.util.Log.d("MovieReviewRepo", "Submitting review: movieId=$tmdbMovieId, rating=$rating, movieTitle='$movieTitle'")
            
            try {
                val response = movieReviewApiService.createReview(request)
                
                // Debug log to check response
                android.util.Log.d("MovieReviewRepo", "Create review response: success=${response.success}, data=${response.data != null}, message=${response.message}")
                
                if (response.success) {
                    android.util.Log.d("MovieReviewRepo", "Review creation successful!")
                    if (response.data != null) {
                        // Ideal case - we have the data
                        android.util.Log.d("MovieReviewRepo", "Emitting success with data: id=${response.data.id}")
                        emit(Resource.Success(response.data.toMovieReview()))
                    } else {
                        // Success but no data - fetch the review manually
                        android.util.Log.d("MovieReviewRepo", "Success but no data, fetching review data")
                        try {
                            val userReviewResponse = movieReviewApiService.getUserReviewForMovie(tmdbMovieId)
                            if (userReviewResponse.data != null) {
                                android.util.Log.d("MovieReviewRepo", "Found review: id=${userReviewResponse.data.id}")
                                emit(Resource.Success(userReviewResponse.data.toMovieReview()))
                            } else {
                                // Create a temporary review object to ensure UI state update
                                android.util.Log.d("MovieReviewRepo", "Couldn't find review, creating placeholder")
                                emit(Resource.Success(
                                    MovieReview(
                                        id = 0, // Will be updated when fetched later
                                        tmdbMovieId = tmdbMovieId,
                                        rating = rating,
                                        content = safeContent,
                                        createdAt = "",
                                        updatedAt = "",
                                        likeCount = 0,
                                        userProfile = UserProfile.empty(),
                                        userHasLiked = false,
                                        movieTitle = movieTitle
                                    )
                                ))
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MovieReviewRepo", "Error fetching review after creation: ${e.message}")
                            // Still emit success to ensure UI is updated
                            android.util.Log.d("MovieReviewRepo", "Still emitting success with placeholder")
                            emit(Resource.Success(
                                MovieReview(
                                    id = 0, 
                                    tmdbMovieId = tmdbMovieId,
                                    rating = rating,
                                    content = safeContent,
                                    createdAt = "",
                                    updatedAt = "",
                                    likeCount = 0,
                                    userProfile = UserProfile.empty(),
                                    userHasLiked = false,
                                    movieTitle = movieTitle
                                )
                            ))
                        }
                    }
                } else {
                    android.util.Log.w("MovieReviewRepo", "API returned success=false: ${response.message}")
                    emit(Resource.Error(response.message ?: "Failed to create review"))
                }
            } catch (e: HttpException) {
                // Check if this is a 409 Conflict (already reviewed)
                if (e.code() == 409) {
                    android.util.Log.d("MovieReviewRepo", "Got 409 conflict - user already reviewed this movie, fetching existing review")
                    
                    try {
                        // Get the existing review for this movie
                        val existingReviewResponse = movieReviewApiService.getUserReviewForMovie(tmdbMovieId)
                        android.util.Log.d("MovieReviewRepo", "Existing review response: success=${existingReviewResponse.success}, data=${existingReviewResponse.data != null}")
                        
                        if (existingReviewResponse.data != null) {
                            val existingReview = existingReviewResponse.data.toMovieReview()
                            android.util.Log.d("MovieReviewRepo", "Found existing review with ID: ${existingReview.id}, updating instead")
                            
                            // Update the existing review
                            val updateResponse = movieReviewApiService.updateReview(
                                existingReview.id,
                                UpdateReviewRequest(
                                    rating = rating,
                                    reviewText = safeContent,
                                    containsSpoilers = containsSpoilers
                                )
                            )
                            
                            android.util.Log.d("MovieReviewRepo", "Update existing review response: success=${updateResponse.success}, data=${updateResponse.data != null}")
                            
                            if (updateResponse.success) {
                                if (updateResponse.data != null) {
                                    emit(Resource.Success(updateResponse.data.toMovieReview()))
                                } else {
                                    // Create a temporary object
                                    emit(Resource.Success(
                                        MovieReview(
                                            id = existingReview.id,
                                            tmdbMovieId = tmdbMovieId,
                                            rating = rating,
                                            content = safeContent,
                                            createdAt = existingReview.createdAt,
                                            updatedAt = "",
                                            likeCount = existingReview.likeCount,
                                            userProfile = existingReview.userProfile,
                                            userHasLiked = existingReview.userHasLiked,
                                            movieTitle = movieTitle
                                        )
                                    ))
                                }
                            } else {
                                emit(Resource.Error(updateResponse.message ?: "Failed to update existing review"))
                            }
                        } else {
                            // Try to find the review ID from the error message or fetch user reviews as fallback
                            val errorBody = e.response()?.errorBody()?.string()
                            android.util.Log.d("MovieReviewRepo", "Conflict error body: $errorBody")
                            
                            emit(Resource.Error("You've already reviewed this movie. Please find your review in My Reviews and update it."))
                        }
                    } catch (fetchError: Exception) {
                        android.util.Log.e("MovieReviewRepo", "Error fetching existing review after conflict: ${fetchError.message}")
                        emit(Resource.Error("You've already reviewed this movie. Please find your review in My Reviews and update it."))
                    }
                } else {
                    // Not a conflict error, pass through
                    emit(Resource.Error(e.message ?: "HTTP error occurred"))
                }
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    override suspend fun updateReview(reviewId: Long, rating: Float, content: String, containsSpoilers: Boolean, tmdbMovieId: Long?, movieTitle: String?): Flow<Resource<MovieReview>> = flow {
        emit(Resource.Loading())
        try {
            val safeContent = content.ifBlank { "" }
            val request = UpdateReviewRequest(
                rating = rating,
                reviewText = safeContent,
                containsSpoilers = containsSpoilers,
                tmdbMovieId = tmdbMovieId,
                movieTitle = movieTitle
            )
            
            android.util.Log.d("MovieReviewRepo", "Updating review: reviewId=$reviewId, rating=$rating, movieTitle=$movieTitle")
            
            val response = movieReviewApiService.updateReview(reviewId, request)
            
            // Debug log
            android.util.Log.d("MovieReviewRepo", "Update review response: success=${response.success}, data=${response.data != null}, message=${response.message}")
            
            if (response.success) {
                if (response.data != null) {
                    android.util.Log.d("MovieReviewRepo", "Review update successful with data")
                    emit(Resource.Success(response.data.toMovieReview()))
                } else {
                    // If success is true but data is null, we need to get the review data
                    android.util.Log.d("MovieReviewRepo", "Review update successful but no data returned")
                    try {
                        // Fetch the updated review
                        val updatedReviewResponse = movieReviewApiService.getReviewById(reviewId)
                        if (updatedReviewResponse.data != null) {
                            emit(Resource.Success(updatedReviewResponse.data.toMovieReview()))
                        } else {
                            // Create a placeholder review object with known data
                            // This ensures we emit success even if we can't fetch the full updated review
                            android.util.Log.d("MovieReviewRepo", "Couldn't fetch updated review, creating placeholder")
                            emit(Resource.Success(
                                MovieReview(
                                    id = reviewId,
                                    tmdbMovieId = tmdbMovieId ?: 0,
                                    rating = rating,
                                    content = safeContent,
                                    createdAt = "",
                                    updatedAt = "",
                                    likeCount = 0,
                                    userProfile = UserProfile.empty(),
                                    userHasLiked = false,
                                    movieTitle = movieTitle
                                )
                            ))
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MovieReviewRepo", "Error fetching updated review: ${e.message}")
                        // Return success even if we can't fetch the updated review
                        // This ensures the UI still transitions to success state
                        emit(Resource.Success(
                            MovieReview(
                                id = reviewId,
                                tmdbMovieId = tmdbMovieId ?: 0, 
                                rating = rating,
                                content = safeContent,
                                createdAt = "",
                                updatedAt = "",
                                likeCount = 0,
                                userProfile = UserProfile.empty(),
                                userHasLiked = false,
                                movieTitle = movieTitle
                            )
                        ))
                    }
                }
            } else {
                emit(Resource.Error(response.message ?: "Failed to update review"))
            }
        } catch (e: HttpException) {
            android.util.Log.e("MovieReviewRepo", "HTTP error in updateReview: ${e.code()}, ${e.message()}, ${e.response()?.errorBody()?.string()}")
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
            // Log lỗi để debug
            android.util.Log.e("MovieReviewRepo", "Error in updateReview: ${e.message}", e)
        }
    }

    override suspend fun deleteReview(reviewId: Long): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            movieReviewApiService.deleteReview(reviewId)
            emit(Resource.Success(Unit))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }
    
    override suspend fun likeReview(reviewId: Long): Flow<Resource<MovieReview>> = flow {
        emit(Resource.Loading())
        try {
            // Log the current state before API call
            val currentReview = try {
                val currentResponse = movieReviewApiService.getReviewById(reviewId)
                currentResponse.data
            } catch (e: Exception) {
                null
            }
            
            android.util.Log.d("MovieReviewRepo", "Before liking - Review $reviewId: " +
                    "likeCount=${currentReview?.likeCount ?: "unknown"}, " +
                    "userHasLiked=${currentReview?.userHasLiked ?: "unknown"}")
            
            // Make the actual like API call
            val response = movieReviewApiService.likeReview(reviewId)
            
            // Log the detailed response
            android.util.Log.d("MovieReviewRepo", "Like review API response: " +
                    "success=${response.success}, " +
                    "message=${response.message ?: "none"}, " +
                    "data=${if (response.data != null) "present" else "null"}")
            
            if (response.data != null) {
                android.util.Log.d("MovieReviewRepo", "Like review successful - Review $reviewId: " +
                        "likeCount=${response.data.likeCount}, " +
                        "userHasLiked=${response.data.userHasLiked}")
                
                val movieReview = response.data.toMovieReview()
                emit(Resource.Success(movieReview))
            } else {
                android.util.Log.w("MovieReviewRepo", "Like review returned null data")
                emit(Resource.Error(response.message ?: "Failed to like review"))
            }
        } catch (e: HttpException) {
            android.util.Log.e("MovieReviewRepo", "HTTP error in likeReview: ${e.code()}, ${e.message()}")
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            android.util.Log.e("MovieReviewRepo", "IO error in likeReview: ${e.message}")
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            android.util.Log.e("MovieReviewRepo", "Error in likeReview: ${e.message}", e)
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }
    
    override suspend fun unlikeReview(reviewId: Long): Flow<Resource<MovieReview>> = flow {
        emit(Resource.Loading())
        try {
            // Log the current state before API call
            val currentReview = try {
                val currentResponse = movieReviewApiService.getReviewById(reviewId)
                currentResponse.data
            } catch (e: Exception) {
                null
            }
            
            android.util.Log.d("MovieReviewRepo", "Before unliking - Review $reviewId: " +
                    "likeCount=${currentReview?.likeCount ?: "unknown"}, " +
                    "userHasLiked=${currentReview?.userHasLiked ?: "unknown"}")
            
            // Make the actual unlike API call
            val response = movieReviewApiService.unlikeReview(reviewId)
            
            // Log the detailed response
            android.util.Log.d("MovieReviewRepo", "Unlike review API response: " +
                    "success=${response.success}, " +
                    "message=${response.message ?: "none"}, " +
                    "data=${if (response.data != null) "present" else "null"}")
            
            if (response.data != null) {
                android.util.Log.d("MovieReviewRepo", "Unlike review successful - Review $reviewId: " +
                        "likeCount=${response.data.likeCount}, " +
                        "userHasLiked=${response.data.userHasLiked}")
                
                val movieReview = response.data.toMovieReview()
                emit(Resource.Success(movieReview))
            } else {
                android.util.Log.w("MovieReviewRepo", "Unlike review returned null data")
                emit(Resource.Error(response.message ?: "Failed to unlike review"))
            }
        } catch (e: HttpException) {
            android.util.Log.e("MovieReviewRepo", "HTTP error in unlikeReview: ${e.code()}, ${e.message()}")
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            android.util.Log.e("MovieReviewRepo", "IO error in unlikeReview: ${e.message}")
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            android.util.Log.e("MovieReviewRepo", "Error in unlikeReview: ${e.message}", e)
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    override suspend fun hasUserReviewedMovie(tmdbMovieId: Long): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieReviewApiService.hasUserReviewedMovie(tmdbMovieId)
            android.util.Log.d("MovieReviewRepo", "Has user reviewed response: success=${response.success}, data=${response.data}")
            
            if (response.data != null) {
                emit(Resource.Success(response.data))
            } else {
                // Nếu backend không trả về data nhưng cũng không lỗi, giả định là false
                emit(Resource.Success(false))
            }
        } catch (e: HttpException) {
            // Nếu API trả về lỗi 404, nghĩa là user chưa review
            if (e.code() == 404) {
                emit(Resource.Success(false))
            } else {
                emit(Resource.Error(e.message ?: "HTTP error occurred"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
            android.util.Log.e("MovieReviewRepo", "Error in hasUserReviewedMovie: ${e.message}", e)
        }
    }

    override suspend fun getUserReviewForMovie(tmdbMovieId: Long): Flow<Resource<MovieReview>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieReviewApiService.getUserReviewForMovie(tmdbMovieId)
            android.util.Log.d("MovieReviewRepo", "Get user review response: success=${response.success}, data=${response.data != null}")
            
            if (response.data != null) {
                emit(Resource.Success(response.data.toMovieReview()))
            } else {
                emit(Resource.Error(response.message ?: "Failed to get user review"))
            }
        } catch (e: HttpException) {
            if (e.code() == 404) {
                // 404 means user hasn't reviewed this movie yet, not a real error
                android.util.Log.d("MovieReviewRepo", "No review found for movie $tmdbMovieId (404)")
                emit(Resource.Error("You haven't reviewed this movie yet"))
            } else {
                emit(Resource.Error(e.message ?: "HTTP error occurred"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
            // Log lỗi để debug
            android.util.Log.e("MovieReviewRepo", "Error in getUserReviewForMovie: ${e.message}", e)
        }
    }

    override suspend fun getFollowingReviews(page: Int, size: Int): Flow<Resource<List<MovieReview>>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieReviewApiService.getFollowingReviews(page, size)
            val reviews = response.content.map { dto -> dto.toMovieReview() }
            emit(Resource.Success(reviews))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }
    
    override suspend fun getPopularReviews(page: Int, size: Int): Flow<Resource<List<MovieReview>>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieReviewApiService.getPopularReviews(page, size)
            val reviews = response.content.map { dto -> dto.toMovieReview() }
            emit(Resource.Success(reviews))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }
    
    override suspend fun getTrendingReviews(page: Int, size: Int): Flow<Resource<List<MovieReview>>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieReviewApiService.getTrendingReviews(page, size)
            val reviews = response.content.map { dto -> dto.toMovieReview() }
            emit(Resource.Success(reviews))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }
}