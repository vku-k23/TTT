package com.ttt.cinevibe.data.repository

import com.ttt.cinevibe.data.remote.models.PageResponse
import com.ttt.cinevibe.data.remote.models.UserProfileResponse
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface UserRecommendationRepository {
    suspend fun getRecommendedUsers(page: Int, size: Int): Flow<Resource<PageResponse<UserProfileResponse>>>
    suspend fun getUserProfile(firebaseUid: String): Flow<Resource<UserProfileResponse>>
    suspend fun searchUsers(query: String, page: Int, size: Int): Flow<Resource<PageResponse<UserProfileResponse>>>
} 