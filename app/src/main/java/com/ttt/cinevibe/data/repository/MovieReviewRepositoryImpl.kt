package com.ttt.cinevibe.data.repository

import com.ttt.cinevibe.data.remote.api.MovieReviewApiService
import com.ttt.cinevibe.data.remote.dto.CreateReviewRequest
import com.ttt.cinevibe.data.remote.dto.UpdateReviewRequest
import com.ttt.cinevibe.domain.model.MovieReview
import com.ttt.cinevibe.domain.model.Resource
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
            
            try {
                val response = movieReviewApiService.createReview(request)
                
                // Debug log để kiểm tra response
                android.util.Log.d("MovieReviewRepo", "Create review response: success=${response.success}, data=${response.data != null}")
                
                if (response.data != null) {
                    // Nếu có data, coi như thành công dù có trường success hay không
                    emit(Resource.Success(response.data.toMovieReview()))
                } else {
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
                            
                            if (updateResponse.data != null) {
                                emit(Resource.Success(updateResponse.data.toMovieReview()))
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

    override suspend fun updateReview(reviewId: Long, rating: Float, content: String, containsSpoilers: Boolean): Flow<Resource<MovieReview>> = flow {
        emit(Resource.Loading())
        try {
            val safeContent = content.ifBlank { "" }
            val request = UpdateReviewRequest(
                rating = rating,
                reviewText = safeContent,
                containsSpoilers = containsSpoilers
            )
            val response = movieReviewApiService.updateReview(reviewId, request)
            
            // Debug log
            android.util.Log.d("MovieReviewRepo", "Update review response: success=${response.success}, data=${response.data != null}")
            
            if (response.data != null) {
                emit(Resource.Success(response.data.toMovieReview()))
            } else {
                emit(Resource.Error(response.message ?: "Failed to update review"))
            }
        } catch (e: HttpException) {
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
            val response = movieReviewApiService.likeReview(reviewId)
            android.util.Log.d("MovieReviewRepo", "Like review response: success=${response.success}, data=${response.data != null}")
            
            if (response.data != null) {
                emit(Resource.Success(response.data.toMovieReview()))
            } else {
                emit(Resource.Error(response.message ?: "Failed to like review"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
            android.util.Log.e("MovieReviewRepo", "Error in likeReview: ${e.message}", e)
        }
    }
    
    override suspend fun unlikeReview(reviewId: Long): Flow<Resource<MovieReview>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieReviewApiService.unlikeReview(reviewId)
            android.util.Log.d("MovieReviewRepo", "Unlike review response: success=${response.success}, data=${response.data != null}")
            
            if (response.data != null) {
                emit(Resource.Success(response.data.toMovieReview()))
            } else {
                emit(Resource.Error(response.message ?: "Failed to unlike review"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
            android.util.Log.e("MovieReviewRepo", "Error in unlikeReview: ${e.message}", e)
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
}