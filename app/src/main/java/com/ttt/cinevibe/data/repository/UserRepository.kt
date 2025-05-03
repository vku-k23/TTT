package com.ttt.cinevibe.data.repository

import com.ttt.cinevibe.data.remote.models.ApiResponse
import com.ttt.cinevibe.data.remote.models.UserResponse
import com.ttt.cinevibe.data.remote.models.UserProfileRequest
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getCurrentUser(): Flow<Resource<UserResponse>>
    suspend fun registerUser(email: String, displayName: String, firebaseUid: String): Flow<Resource<UserResponse>>
    suspend fun updateUser(updateRequest: UserProfileRequest): Flow<Resource<UserResponse>>
    suspend fun deleteUser(): Flow<Resource<ApiResponse>>
}