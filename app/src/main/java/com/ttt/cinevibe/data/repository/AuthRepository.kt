package com.ttt.cinevibe.data.repository

import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun loginUser(email: String, password: String): Flow<Resource<Boolean>>
    fun registerUser(email: String, password: String, username: String): Flow<Resource<Boolean>>
    fun logoutUser(): Flow<Resource<Boolean>>
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
}