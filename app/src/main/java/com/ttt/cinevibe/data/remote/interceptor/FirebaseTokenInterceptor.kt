package com.ttt.cinevibe.data.remote.interceptor

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTokenInterceptor @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val currentUser = firebaseAuth.currentUser
        
        // Only proceed with token if user is logged in
        return if (currentUser != null) {
            try {
                // Get token synchronously - in real apps consider caching the token
                val token = runCatching { 
                    kotlinx.coroutines.runBlocking { 
                        currentUser.getIdToken(false).await().token 
                    }
                }.getOrNull()
                
                if (token != null) {
                    // Add Authorization header with Bearer token
                    val authenticatedRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                    chain.proceed(authenticatedRequest)
                } else {
                    // Proceed with original request if token is null
                    android.util.Log.w("FirebaseTokenInterceptor", "Couldn't get token, proceeding without authentication")
                    chain.proceed(originalRequest)
                }
            } catch (e: Exception) {
                // Log error but don't fail the request
                android.util.Log.e("FirebaseTokenInterceptor", "Error getting Firebase token: ${e.message}")
                chain.proceed(originalRequest)
            }
        } else {
            chain.proceed(originalRequest)
        }
    }
}