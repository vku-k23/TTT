package com.ttt.cinevibe.data.remote.api

import com.ttt.cinevibe.data.remote.models.UserRequest
import com.ttt.cinevibe.data.remote.models.UserResponse
import com.ttt.cinevibe.data.remote.models.UserProfileRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApiService {
    
    @GET("api/user/me")
    suspend fun getCurrentUser(): UserResponse
    
    @POST("api/user/sync")
    suspend fun syncUser(@Body userRequest: UserRequest): UserResponse
    
    @PUT("api/user/me")
    suspend fun updateUserProfile(@Body profileRequest: UserProfileRequest): UserResponse
}