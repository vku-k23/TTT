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
        
        // List of path patterns that can be accessed without authentication
        private val PUBLIC_API_PATHS = listOf(
            "/api/connections/users/.*/followers", 
            "/api/connections/users/.*/following"
        )
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestUrl = originalRequest.url.toString()
        val currentUser = firebaseAuth.currentUser
        
        // Log the request URL for debugging
        Log.d(TAG, "Intercepting request to: $requestUrl")
        
        // Check if the request URL is for a public API
        if (isPublicApiPath(requestUrl)) {
            Log.d(TAG, "Public API path detected, proceeding without authentication: $requestUrl")
            return chain.proceed(originalRequest)
        }
        
        // Only proceed with token if user is logged in
        return if (currentUser != null) {
            try {
                Log.d(TAG, "Current user found. UID: ${currentUser.uid}")
                
                // Get username from preferences if available
                val username = runBlocking { userPreferences.getUsername() }
                
                // Force reload user before getting token to ensure metadata is up to date
                // This can help with synchronization issues
                runBlocking {
                    try {
                        currentUser.reload().await()
                        Log.d(TAG, "User metadata reloaded successfully")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to reload user metadata: ${e.message}")
                        // Continue anyway
                    }
                }
                
                // Get token with force refresh to ensure it's current
                val tokenTask = runBlocking { 
                    try {
                        // Force refresh token to ensure it's synchronized with Firebase servers
                        val result = currentUser.getIdToken(true).await()
                        val token = result.token
                        
                        // Log token details for debugging
                        val claims = result.claims
                        val issuedAt = claims["iat"] as? Long ?: 0
                        val expiresAt = claims["exp"] as? Long ?: 0
                        val currentTime = System.currentTimeMillis() / 1000
                        
                        Log.d(TAG, "Token details - issued at: $issuedAt, expires at: $expiresAt, " +
                             "current time: $currentTime, time since issued: ${currentTime - issuedAt} seconds")
                         
                        // Check if we have a severe clock skew issue
                        if (issuedAt > currentTime) {
                            val timeDiff = issuedAt - currentTime
                            Log.w(TAG, "⚠️ Clock skew detected! Token issued at is ${timeDiff} seconds in the future!" +
                                  "This may cause authentication issues with the backend.")
                        }
                        
                        token
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting fresh token: ${e.message}", e)
                        // Fallback to non-refreshed token as last resort
                        currentUser.getIdToken(false).await().token
                    }
                }
                
                if (tokenTask != null) {
                    // Create request builder with the Firebase token
                    val requestBuilder = originalRequest.newBuilder()
                        .header(BackendApiConstants.AUTH_HEADER, "${BackendApiConstants.AUTH_BEARER_PREFIX}$tokenTask")
                    
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
            Log.w(TAG, "No user logged in, proceeding without authentication: $requestUrl")
            chain.proceed(originalRequest)
        }
    }

    /**
     * Check if the request URL is for a public API that doesn't require authentication
     */
    private fun isPublicApiPath(url: String): Boolean {
        return PUBLIC_API_PATHS.any { pattern ->
            // Convert the pattern to a regex
            val regex = pattern.replace(".", "\\.").replace("*", ".*")
            url.contains(regex.toRegex())
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