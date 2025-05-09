package com.ttt.cinevibe.data.repository

import com.ttt.cinevibe.data.remote.api.UserRecommendationApiService
import com.ttt.cinevibe.data.remote.models.PageResponse
import com.ttt.cinevibe.data.remote.models.UserProfileResponse
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRecommendationRepositoryImpl @Inject constructor(
    private val userRecommendationApiService: UserRecommendationApiService
) : UserRecommendationRepository {

    override suspend fun getRecommendedUsers(
        page: Int,
        size: Int
    ): Flow<Resource<PageResponse<UserProfileResponse>>> = flow {
        emit(Resource.Loading())
        try {
            val response = userRecommendationApiService.getRecommendedUsers(page, size)
            emit(Resource.Success(response))
        } catch (e: Exception) {
            val errorMessage = getErrorMessage(e)
            emit(Resource.Error(errorMessage))
        }
    }

    override suspend fun getUserProfile(
        firebaseUid: String
    ): Flow<Resource<UserProfileResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = userRecommendationApiService.getUserProfile(firebaseUid)
            emit(Resource.Success(response))
        } catch (e: Exception) {
            val errorMessage = getErrorMessage(e)
            emit(Resource.Error(errorMessage))
        }
    }

    override suspend fun searchUsers(
        query: String,
        page: Int,
        size: Int
    ): Flow<Resource<PageResponse<UserProfileResponse>>> = flow {
        emit(Resource.Loading())
        try {
            val response = userRecommendationApiService.searchUsers(query, page, size)
            emit(Resource.Success(response))
        } catch (e: Exception) {
            val errorMessage = getErrorMessage(e)
            emit(Resource.Error(errorMessage))
        }
    }
    
    private fun getErrorMessage(e: Exception): String {
        return when (e) {
            is retrofit2.HttpException -> "Server error: ${e.code()} - ${e.message()}"
            is java.net.SocketTimeoutException -> "Connection timeout - please check your internet"
            is java.io.IOException -> "Network error - please check your connection"
            else -> e.message ?: "Unknown error occurred"
        }
    }
} 