package com.ttt.cinevibe.data.remote.interceptor

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ttt.cinevibe.data.local.UserPreferences
import com.ttt.cinevibe.data.remote.BackendApiConstants
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTokenInterceptor @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userPreferences: UserPreferences
) : Interceptor {
    
    companion object {
        private const val TAG = "FirebaseTokenInterceptor"
        private const val HEADER_USERNAME = "X-Username"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val currentUser = firebaseAuth.currentUser
        
        // Log the request URL for debugging
        Log.d(TAG, "Intercepting request to: ${originalRequest.url}")
        
        // Only proceed with token if user is logged in
        return if (currentUser != null) {
            try {
                Log.d(TAG, "Current user found. UID: ${currentUser.uid}")
                
                // Get username from preferences if available
                val username = runBlocking { userPreferences.getUsername() }
                
                // Get token (force refresh to ensure it's current)
                val token = runBlocking { 
                    currentUser.getIdToken(true).await().token 
                }
                
                if (token != null) {
                    // Create request builder with the Firebase token
                    val requestBuilder = originalRequest.newBuilder()
                        .header(BackendApiConstants.AUTH_HEADER, "${BackendApiConstants.AUTH_BEARER_PREFIX}$token")
                    
                    // Add username as a custom header if available
                    if (!username.isNullOrEmpty()) {
                        Log.d(TAG, "Adding username header: $username")
                        requestBuilder.header(HEADER_USERNAME, username)
                    } else {
                        Log.w(TAG, "No username available in preferences")
                    }
                    
                    val authenticatedRequest = requestBuilder.build()
                    Log.d(TAG, "Added authentication to request: ${originalRequest.url}")
                    
                    chain.proceed(authenticatedRequest)
                } else {
                    Log.e(TAG, "Authentication required but couldn't get valid token")
                    throw IOException("Authentication required but failed to get valid token")
                }
            } catch (e: Exception) {
                // Log error but don't fail the request silently
                Log.e(TAG, "Error getting Firebase token: ${e.message}", e)
                throw IOException("Failed to authenticate with backend: ${e.message}", e)
            }
        } else {
            // User not logged in, proceed without authentication
            Log.w(TAG, "No user logged in, proceeding without authentication: ${originalRequest.url}")
            chain.proceed(originalRequest)
        }
    }
    
    /**
     * Get a token that includes the username in the claims.
     * This is done by ensuring the username is set in the user's display name field
     * since we can't directly modify JWT claims from client.
     */
    private suspend fun getTokenWithUsername(user: FirebaseUser, username: String): String? {
        try {
            // When we force refresh, Firebase will generate a new token with the metadata
            // from the user record, which includes the display name
            return user.getIdToken(true).await().token
        } catch (e: Exception) {
            Log.e(TAG, "Error getting token with username: ${e.message}", e)
            return null
        }
    }
}