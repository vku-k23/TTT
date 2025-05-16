package com.ttt.cinevibe.data.repository

import com.ttt.cinevibe.data.remote.api.UserApiService
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
    
    // Cache the current user to avoid unnecessary API calls
    private var cachedCurrentUser: Resource<UserResponse>? = null
    private var lastFetchTime: Long = 0
    private val CACHE_TIMEOUT = 60000 // 1 minute in milliseconds
    
    override suspend fun getCurrentUser(forceRefresh: Boolean): Flow<Resource<UserResponse>> = flow {
        // Check if we can use cache
        val currentTime = System.currentTimeMillis()
        val cacheExpired = (currentTime - lastFetchTime) > CACHE_TIMEOUT
        val shouldRefresh = forceRefresh || cacheExpired || cachedCurrentUser == null
        
        if (!shouldRefresh && cachedCurrentUser is Resource.Success) {
            android.util.Log.d("UserRepository", "Using cached user data")
            emit(cachedCurrentUser!!)
            return@flow
        }
        
        emit(Resource.Loading())
        try {
            android.util.Log.d("UserRepository", "Making API call to getCurrentUser()")
            val response = userApiService.getCurrentUser()
            android.util.Log.d("UserRepository", "Received user response: $response")
            // Cache the successful response and update last fetch time
            cachedCurrentUser = Resource.Success(response)
            lastFetchTime = System.currentTimeMillis()
            emit(Resource.Success(response))
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error getting current user", e)
            
            // Check for specific error types
            val errorMessage = when (e) {
                is retrofit2.HttpException -> "Server error: ${e.code()} - ${e.message()}"
                is java.net.SocketTimeoutException -> "Connection timeout - please check your internet"
                is java.io.IOException -> "Network error - please check your connection"
                else -> e.message ?: "Failed to get current user"
            }
            
            val error = Resource.Error<UserResponse>(errorMessage)
            // Only update cache with error if we don't have any previous successful data
            if (cachedCurrentUser !is Resource.Success) {
                cachedCurrentUser = error
            }
            emit(error)
        }
    }
    
    override fun getCurrentUserSync(): Resource<UserResponse> {
        // Return cached user if available, otherwise return an error
        return cachedCurrentUser ?: Resource.Error("User not loaded yet")
    }
    
    override fun invalidateCache() {
        android.util.Log.d("UserRepository", "Invalidating user cache")
        lastFetchTime = 0
        // Clear cached user data entirely to force a complete refresh
        cachedCurrentUser = null
    }

    override suspend fun syncUser(
        email: String, 
        displayName: String,
        username: String,
        firebaseUid: String
    ): Flow<Resource<UserResponse>> = flow {
        emit(Resource.Loading())
        try {
            android.util.Log.d("UserRepository", "Making API call to syncUser() with username: $username")
            val userRequest = UserRequest(
                email = email,
                displayName = displayName,
                username = username,
                firebaseUid = firebaseUid
            )
            android.util.Log.d("UserRepository", "Sending user request: $userRequest")
            val response = userApiService.syncUser(userRequest)
            android.util.Log.d("UserRepository", "Sync user successful: $response")
            
            // Update cache with new data
            cachedCurrentUser = Resource.Success(response)
            lastFetchTime = System.currentTimeMillis()
            
            emit(Resource.Success(response))
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error syncing user", e)
            
            // Check for specific error types
            val errorMessage = when (e) {
                is retrofit2.HttpException -> "Server error: ${e.code()} - ${e.message()}"
                is kotlinx.serialization.SerializationException -> "Data parsing error: ${e.message}"
                is java.net.SocketTimeoutException -> "Connection timeout - please check your internet"
                is java.io.IOException -> "Network error - please check your connection"
                else -> e.message ?: "Failed to sync user with backend"
            }
            
            emit(Resource.Error(errorMessage))
        }
    }

    override suspend fun updateUserProfile(profileRequest: UserProfileRequest): Flow<Resource<UserResponse>> = flow {
        emit(Resource.Loading())
        try {
            android.util.Log.d("UserRepository", "Making API call to updateUserProfile(): $profileRequest")
            val response = userApiService.updateUserProfile(profileRequest)
            android.util.Log.d("UserRepository", "Update profile successful: $response")
            
            // Immediately update the cache with the new profile data
            cachedCurrentUser = Resource.Success(response)
            lastFetchTime = System.currentTimeMillis()
            
            emit(Resource.Success(response))
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error updating user profile", e)
            
            // Check for specific error types
            val errorMessage = when (e) {
                is retrofit2.HttpException -> "Server error: ${e.code()} - ${e.message()}"
                is kotlinx.serialization.SerializationException -> "Data parsing error: ${e.message}"
                is java.net.SocketTimeoutException -> "Connection timeout - please check your internet"
                is java.io.IOException -> "Network error - please check your connection"
                else -> e.message ?: "Failed to update user profile"
            }
            
            emit(Resource.Error(errorMessage))
        }
    }
}