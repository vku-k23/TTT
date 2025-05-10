package com.ttt.cinevibe.data.remote.api

import com.ttt.cinevibe.data.remote.models.PageResponse
import com.ttt.cinevibe.data.remote.models.UserConnectionRequest
import com.ttt.cinevibe.data.remote.models.UserConnectionResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserConnectionApiService {
    
    @GET("/api/connections/following")
    suspend fun getFollowing(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageResponse<UserConnectionResponse>
    
    @GET("/api/connections/followers")
    suspend fun getFollowers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageResponse<UserConnectionResponse>
    
    @GET("/api/connections/users/{userId}/following")
    suspend fun getUserFollowing(
        @Path("userId") userId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageResponse<UserConnectionResponse>
    
    @GET("/api/connections/users/{userId}/followers")
    suspend fun getUserFollowers(
        @Path("userId") userId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageResponse<UserConnectionResponse>
    
    @GET("/api/connections/pending")
    suspend fun getPendingRequests(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageResponse<UserConnectionResponse>
    
    @POST("/api/connections/follow")
    suspend fun followUser(
        @Body request: UserConnectionRequest
    ): UserConnectionResponse
    
    @POST("/api/connections/{connectionId}/accept")
    suspend fun acceptFollowRequest(
        @Path("connectionId") connectionId: Long
    ): UserConnectionResponse
    
    @POST("/api/connections/{connectionId}/reject")
    suspend fun rejectFollowRequest(
        @Path("connectionId") connectionId: Long
    )
    
    @DELETE("/api/connections/unfollow/{targetUserUid}")
    suspend fun unfollowUser(
        @Path("targetUserUid") targetUserUid: String
    )
    
    @DELETE("/api/connections/followers/{followerUid}")
    suspend fun removeFollower(
        @Path("followerUid") followerUid: String
    )
    
    @GET("/api/connections/check/{targetUserUid}")
    suspend fun checkConnectionStatus(
        @Path("targetUserUid") targetUserUid: String
    ): Map<String, Any>

    @DELETE("/api/connections/cancel-request/{targetUserUid}")
    suspend fun cancelFollowRequest(
        @Path("targetUserUid") targetUserUid: String
    )
}