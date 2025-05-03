package com.ttt.cinevibe.data.remote.api

import com.ttt.cinevibe.data.remote.models.ApiResponse
import com.ttt.cinevibe.data.remote.models.UserRequest
import com.ttt.cinevibe.data.remote.models.UserResponse
import com.ttt.cinevibe.data.remote.models.UserProfileRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApiService {
    
    @GET("api/user/me")
    suspend fun getCurrentUser(): UserResponse
    
    @POST("api/user/register")
    suspend fun registerUser(@Body userRequest: UserRequest): UserResponse
    
    @PUT("api/user/profile")
    suspend fun updateUser(@Body updateRequest: UserProfileRequest): UserResponse
    
    @DELETE("api/user/me")
    suspend fun deleteUser(): ApiResponse
    
}