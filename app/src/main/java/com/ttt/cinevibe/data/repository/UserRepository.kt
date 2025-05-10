package com.ttt.cinevibe.data.repository

import com.ttt.cinevibe.data.remote.models.UserResponse
import com.ttt.cinevibe.data.remote.models.UserProfileRequest
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getCurrentUser(forceRefresh: Boolean = false): Flow<Resource<UserResponse>>
    fun getCurrentUserSync(): Resource<UserResponse>
    fun invalidateCache()
    suspend fun syncUser(email: String, displayName: String, username: String, firebaseUid: String): Flow<Resource<UserResponse>>
    suspend fun updateUserProfile(profileRequest: UserProfileRequest): Flow<Resource<UserResponse>>
}