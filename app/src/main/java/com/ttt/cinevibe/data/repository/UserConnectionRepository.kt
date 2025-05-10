package com.ttt.cinevibe.data.repository

import com.ttt.cinevibe.data.remote.models.PageResponse
import com.ttt.cinevibe.data.remote.models.UserConnectionRequest
import com.ttt.cinevibe.data.remote.models.UserConnectionResponse
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface UserConnectionRepository {
    
    suspend fun getFollowing(page: Int, size: Int): Flow<Resource<PageResponse<UserConnectionResponse>>>
    
    suspend fun getFollowers(page: Int, size: Int): Flow<Resource<PageResponse<UserConnectionResponse>>>
    
    suspend fun getUserFollowing(userId: String, page: Int, size: Int): Flow<Resource<PageResponse<UserConnectionResponse>>>
    
    suspend fun getUserFollowers(userId: String, page: Int, size: Int): Flow<Resource<PageResponse<UserConnectionResponse>>>
    
    suspend fun getPendingRequests(page: Int, size: Int): Flow<Resource<PageResponse<UserConnectionResponse>>>
    
    suspend fun followUser(targetUserUid: String): Flow<Resource<UserConnectionResponse>>
    
    suspend fun acceptFollowRequest(connectionId: Long): Flow<Resource<UserConnectionResponse>>
    
    suspend fun rejectFollowRequest(connectionId: Long): Flow<Resource<Boolean>>
    
    suspend fun unfollowUser(targetUserUid: String): Flow<Resource<Boolean>>
    
    suspend fun removeFollower(followerUid: String): Flow<Resource<Boolean>>
    
    suspend fun checkConnectionStatus(targetUserUid: String): Flow<Resource<Map<String, Any>>>
    
    suspend fun cancelFollowRequest(targetUserUid: String): Flow<Resource<Boolean>>
}