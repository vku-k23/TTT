package com.ttt.cinevibe.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ttt.cinevibe.data.remote.api.UserApiService
import com.ttt.cinevibe.data.remote.models.UserResponse
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Example API client helper that demonstrates how to make authenticated API calls to the backend.
 * This provides a simplified way to call backend APIs for sample code.
 */
@Singleton
class BackendApiClient @Inject constructor(
    private val userApiService: UserApiService,
    private val firebaseAuth: FirebaseAuth
) {
    /**
     * Gets the current user profile from the backend.
     * The Firebase ID token is automatically added by [FirebaseTokenInterceptor].
     */
    suspend fun getCurrentUser(): Result<UserResponse> = runCatching {
        userApiService.getCurrentUser()
    }
    
    /**
     * Registers the user with the backend after successful Firebase authentication.
     */
    suspend fun registerWithBackend(username: String): Result<UserResponse> {
        val currentUser = firebaseAuth.currentUser ?: return Result.failure(
            IllegalStateException("No authenticated user found")
        )
        
        val firebaseUid = currentUser.uid
        val email = currentUser.email ?: return Result.failure(
            IllegalStateException("User email not available")
        )
        
        return runCatching {
            val userRequest = UserRequest(
                email = email,
                username = username,
                firebaseUid = firebaseUid
            )
            userApiService.registerUser(userRequest)
        }
    }
    
    /**
     * Example method for retrieving a Firebase token for manual API calls.
     * Note: Normally you should use the [FirebaseTokenInterceptor] instead.
     */
    suspend fun getFirebaseToken(forceRefresh: Boolean = false): Result<String> = runCatching {
        val user = firebaseAuth.currentUser ?: throw IllegalStateException("No authenticated user")
        user.getIdToken(forceRefresh).await().token 
            ?: throw IllegalStateException("Could not retrieve Firebase token")
    }
}

/**
 * Sample model for registering a user. Same as the one in models package but added
 * here for demonstration purposes.
 */
data class UserRequest(
    val email: String,
    val username: String,
    val firebaseUid: String
)