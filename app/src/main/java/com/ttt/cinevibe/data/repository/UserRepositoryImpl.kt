package com.ttt.cinevibe.data.repository

import com.ttt.cinevibe.data.remote.api.UserApiService
import com.ttt.cinevibe.data.remote.models.ApiResponse
import com.ttt.cinevibe.data.remote.models.UserRequest
import com.ttt.cinevibe.data.remote.models.UserResponse
import com.ttt.cinevibe.data.remote.models.UserProfileRequest
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService
) : UserRepository {
    
    override suspend fun getCurrentUser(): Flow<Resource<UserResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = userApiService.getCurrentUser()
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get current user"))
        }
    }

    override suspend fun registerUser(
        email: String, 
        displayName: String, 
        firebaseUid: String
    ): Flow<Resource<UserResponse>> = flow {
        emit(Resource.Loading())
        try {
            val userRequest = UserRequest(
                email = email,
                displayName = displayName,
                firebaseUid = firebaseUid
            )
            val response = userApiService.registerUser(userRequest)
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Registration with backend failed"))
        }
    }

    override suspend fun updateUser(updateRequest: UserProfileRequest): Flow<Resource<UserResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = userApiService.updateUser(updateRequest)
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update user"))
        }
    }

    override suspend fun deleteUser(): Flow<Resource<ApiResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = userApiService.deleteUser()
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to delete user"))
        }
    }
}