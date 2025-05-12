package com.ttt.cinevibe.data.repository

import com.ttt.cinevibe.data.remote.api.MovieReviewApiService
import com.ttt.cinevibe.data.remote.models.MovieReviewRequest
import com.ttt.cinevibe.data.remote.models.MovieReviewResponse
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
            val reviews = response.content.map { it.toDomainModel() }
            emit(Resource.Success(reviews))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred"))
        }
    }

    override suspend fun getMovieReviews(tmdbMovieId: Long, page: Int, size: Int): Flow<Resource<List<MovieReview>>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieReviewApiService.getMovieReviews(tmdbMovieId, page, size)
            val reviews = response.content.map { it.toDomainModel() }
            emit(Resource.Success(reviews))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred"))
        }
    }

    override suspend fun getReviewById(reviewId: Long): Flow<Resource<MovieReview>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieReviewApiService.getReviewById(reviewId)
            emit(Resource.Success(response.toDomainModel()))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred"))
        }
    }

    override suspend fun createReview(tmdbMovieId: Long, rating: Int, content: String): Flow<Resource<MovieReview>> = flow {
        emit(Resource.Loading())
        try {
            val request = MovieReviewRequest(tmdbMovieId, rating, content)
            val response = movieReviewApiService.createReview(request)
            emit(Resource.Success(response.toDomainModel()))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred"))
        }
    }

    override suspend fun updateReview(reviewId: Long, rating: Int, content: String): Flow<Resource<MovieReview>> = flow {
        emit(Resource.Loading())
        try {
            val currentReview = movieReviewApiService.getReviewById(reviewId)
            val request = MovieReviewRequest(currentReview.tmdbMovieId, rating, content)
            val response = movieReviewApiService.updateReview(reviewId, request)
            emit(Resource.Success(response.toDomainModel()))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
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
            emit(Resource.Error("An unexpected error occurred"))
        }
    }

    override suspend fun likeReview(reviewId: Long): Flow<Resource<MovieReview>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieReviewApiService.likeReview(reviewId)
            emit(Resource.Success(response.toDomainModel()))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred"))
        }
    }

    override suspend fun unlikeReview(reviewId: Long): Flow<Resource<MovieReview>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieReviewApiService.unlikeReview(reviewId)
            emit(Resource.Success(response.toDomainModel()))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred"))
        }
    }

    override suspend fun hasUserReviewedMovie(tmdbMovieId: Long): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = movieReviewApiService.hasUserReviewedMovie(tmdbMovieId)
            emit(Resource.Success(response.hasReviewed))
        } catch (e: HttpException) {
            emit(Resource.Error(e.message ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred"))
        }
    }

    // Extension function to convert API response model to domain model
    private fun MovieReviewResponse.toDomainModel(): MovieReview {
        return MovieReview(
            id = this.id,
            tmdbMovieId = this.tmdbMovieId,
            content = this.content,
            rating = this.rating,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            userProfile = UserProfile(
                uid = this.userProfile.firebaseUid,
                displayName = this.userProfile.displayName,
                email = this.userProfile.email,
                avatarUrl = this.userProfile.profileImageUrl
            ),
            likeCount = this.likeCount,
            userHasLiked = this.userHasLiked
        )
    }
}