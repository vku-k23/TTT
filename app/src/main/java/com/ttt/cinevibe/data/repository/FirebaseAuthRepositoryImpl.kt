package com.ttt.cinevibe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.ttt.cinevibe.data.local.UserPreferences
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userPreferences: UserPreferences
) : AuthRepository {

    override fun forgotPassword(email: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            firebaseAuth.sendPasswordResetEmail(email).await()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to send password reset email"))
        }
    }

    override fun loginUser(email: String, password: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            // Clear previous user data before attempting a new login
            clearUserLocalData()
            
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val isSuccessful = result.user != null
            emit(Resource.Success(isSuccessful))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun registerUser(email: String, password: String, displayName: String, username: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            // Clear previous user data before registering a new user
            clearUserLocalData()
            
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            
            result.user?.let { user ->
                // Update display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                user.updateProfile(profileUpdates).await()
                
                // Store additional user info in Firestore
                val userData = hashMapOf(
                    "userId" to user.uid,
                    "email" to email,
                    "displayName" to displayName,
                    "username" to username,
                    "createdAt" to System.currentTimeMillis()
                )
                
                // Log what we're saving to debug if needed
                android.util.Log.d("FirebaseAuthRepo", "Storing user data in Firestore: userId=${user.uid}, username=$username")
                
                firestore.collection("users")
                    .document(user.uid)
                    .set(userData)
                    .await()
                
                emit(Resource.Success(true))
            } ?: emit(Resource.Error("Registration failed"))
            
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun logoutUser(): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            
            // Clear local data first
            clearUserLocalData()
            
            // Then sign out from Firebase
            firebaseAuth.signOut()
            
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error logging out"))
        }
    }
    
    // Helper method to clear local user data
    private suspend fun clearUserLocalData() {
        try {
            userPreferences.clearUserData()
            android.util.Log.d("FirebaseAuthRepo", "Cleared local user preferences")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseAuthRepo", "Error clearing user preferences: ${e.message}", e)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }
}