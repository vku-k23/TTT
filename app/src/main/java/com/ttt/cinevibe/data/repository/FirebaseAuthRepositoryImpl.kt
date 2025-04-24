package com.ttt.cinevibe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun loginUser(email: String, password: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val isSuccessful = result.user != null
            emit(Resource.Success(isSuccessful))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun registerUser(email: String, password: String, username: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            
            result.user?.let { user ->
                // Update display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
                user.updateProfile(profileUpdates).await()
                
                // Store additional user info in Firestore
                val userData = hashMapOf(
                    "userId" to user.uid,
                    "email" to email,
                    "username" to username,
                    "createdAt" to System.currentTimeMillis()
                )
                
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
            firebaseAuth.signOut()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error logging out"))
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