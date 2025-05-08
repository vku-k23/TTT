package com.ttt.cinevibe.data.remote.interceptor

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.ttt.cinevibe.data.remote.BackendApiConstants
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTokenInterceptor @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : Interceptor {
    
    companion object {
        private const val TAG = "FirebaseTokenInterceptor"
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
                // Always get a fresh token (forceRefresh=true) for better security
                val token = runCatching { 
                    kotlinx.coroutines.runBlocking { 
                        Log.d(TAG, "Getting fresh ID token...")
                        currentUser.getIdToken(true).await().token 
                    }
                }.getOrNull()
                
                if (token != null) {
                    // Add Authorization header with Bearer token
                    val authenticatedRequest = originalRequest.newBuilder()
                        .header(BackendApiConstants.AUTH_HEADER, "${BackendApiConstants.AUTH_BEARER_PREFIX}$token")
                        .build()
                    Log.d(TAG, "Added Firebase token to request: ${originalRequest.url}")
                    chain.proceed(authenticatedRequest)
                } else {
                    // Fail the request if token is null - this is an auth protected endpoint
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
}