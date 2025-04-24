package com.ttt.cinevibe.domain.usecase.auth

import com.ttt.cinevibe.data.repository.AuthRepository
import javax.inject.Inject

class GetAuthStatusUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    fun isUserLoggedIn(): Boolean {
        return repository.isUserLoggedIn()
    }
    
    fun getCurrentUserId(): String? {
        return repository.getCurrentUserId()
    }
    
    fun getCurrentUserEmail(): String? {
        return repository.getCurrentUserEmail()
    }
}