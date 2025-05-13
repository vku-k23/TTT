package com.ttt.cinevibe.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ttt.cinevibe.data.remote.api.UserApiService
import com.ttt.cinevibe.data.remote.models.UserRequest
import com.ttt.cinevibe.data.remote.models.UserResponse
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API client helper that makes authenticated API calls to the backend.
 * Ensures proper token handling and authentication flow.
 */
@Singleton
class BackendApiClient @Inject constructor(
    private val userApiService: UserApiService,
    private val firebaseAuth: FirebaseAuth
) {
    companion object {
        private const val TAG = "BackendApiClient"
    }
    
    /**
     * Gets the current user profile from the backend.
     * The Firebase ID token is automatically added by [FirebaseTokenInterceptor].
     */
    suspend fun getCurrentUser(): Result<UserResponse> = runCatching {
        // Ensure we have a fresh Firebase user before API calls
        refreshCurrentUser()
        
        val firebaseUser = firebaseAuth.currentUser
            ?: throw IllegalStateException("No authenticated user found")
        
        Log.d(TAG, "Fetching user profile for UID: ${firebaseUser.uid}")
        
        try {
            userApiService.getCurrentUser()
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error getting current user: ${e.code()}", e)
            when (e.code()) {
                401 -> throw IllegalStateException("Authentication failed. Please log in again.")
                403 -> throw IllegalStateException("You don't have permission to access this resource.")
                404 -> throw IllegalStateException("User profile not found. You may need to create one.")
                else -> throw e
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error getting current user", e)
            throw IOException("Network error: Please check your internet connection")
        }
    }

    /**
     * Syncs the user with the backend after successful Firebase authentication.
     */
    suspend fun registerWithBackend(displayName: String, username: String): Result<UserResponse> {
        return runCatching {
            // Ensure we have a fresh Firebase user
            refreshCurrentUser()
            
            val firebaseUser = firebaseAuth.currentUser 
                ?: throw IllegalStateException("No authenticated user found")
            
            val firebaseUid = firebaseUser.uid
            val email = firebaseUser.email 
                ?: throw IllegalStateException("User email not available")
            
            Log.d(TAG, "Registering with backend - UID: $firebaseUid, email: $email, username: $username")
            
            val userRequest = UserRequest(
                email = email,
                displayName = displayName,
                username = username,
                firebaseUid = firebaseUid
            )
            
            try {
                userApiService.syncUser(userRequest)
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error during backend registration: ${e.code()}", e)
                when (e.code()) {
                    409 -> throw IllegalStateException("Username already exists. Please choose another username.")
                    401 -> throw IllegalStateException("Authentication failed. Please log in again.")
                    else -> throw e
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error during backend registration", e)
                throw IOException("Network error: Please check your internet connection")
            }
        }
    }

    /**
     * Get a Firebase token, always with force refresh to ensure it's current.
     * This helps prevent issues with expired or invalid tokens.
     */
    suspend fun getFirebaseToken(forceRefresh: Boolean = true): Result<String> = runCatching {
        refreshCurrentUser()
        
        val firebaseUser = firebaseAuth.currentUser 
            ?: throw IllegalStateException("No authenticated user")
        
        val tokenResult = firebaseUser.getIdToken(forceRefresh).await()
        val token = tokenResult.token
            ?: throw IllegalStateException("Could not retrieve Firebase token")
        
        // Log token metadata for debugging
        val claims = tokenResult.claims
        val issuedAt = claims["iat"] as? Long ?: 0
        val expiresAt = claims["exp"] as? Long ?: 0
        val currentTime = System.currentTimeMillis() / 1000
        
        Log.d(TAG, "Retrieved fresh token - issued at: $issuedAt, expires at: $expiresAt, " +
             "current time: $currentTime, TTL: ${expiresAt - currentTime}s")
        
        token
    }
    
    /**
     * Force reload the current Firebase user to ensure fresh data
     */
    private suspend fun refreshCurrentUser() {
        val firebaseUser = firebaseAuth.currentUser ?: return
        
        try {
            firebaseUser.reload().await()
            Log.d(TAG, "Successfully reloaded Firebase user: ${firebaseUser.uid}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to reload Firebase user: ${e.message}")
            // Continue anyway, as reload failure shouldn't block API calls
        }
    }
}
