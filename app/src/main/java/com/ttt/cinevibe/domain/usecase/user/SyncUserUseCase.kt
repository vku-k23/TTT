package com.ttt.cinevibe.domain.usecase.user

import com.ttt.cinevibe.data.remote.models.UserResponse
import com.ttt.cinevibe.data.repository.UserRepository
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        email: String,
        displayName: String,
        username: String,
        firebaseUid: String
    ): Flow<Resource<UserResponse>> {
        return userRepository.syncUser(email, displayName, username, firebaseUid)
    }
}