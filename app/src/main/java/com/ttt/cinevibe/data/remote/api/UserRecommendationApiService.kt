package com.ttt.cinevibe.data.remote.api

import com.ttt.cinevibe.data.remote.models.PageResponse
import com.ttt.cinevibe.data.remote.models.UserProfileResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserRecommendationApiService {
    
    @GET("api/users/recommendations")
    suspend fun getRecommendedUsers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PageResponse<UserProfileResponse>
    
    @GET("api/users/{firebaseUid}")
    suspend fun getUserProfile(
        @Path("firebaseUid") firebaseUid: String
    ): UserProfileResponse
    
    @GET("api/users/search")
    suspend fun searchUsers(
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PageResponse<UserProfileResponse>
} 