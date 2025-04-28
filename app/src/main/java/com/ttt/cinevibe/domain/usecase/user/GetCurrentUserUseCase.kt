package com.ttt.cinevibe.domain.usecase.user

import com.ttt.cinevibe.data.remote.models.UserResponse
import com.ttt.cinevibe.data.repository.UserRepository
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Flow<Resource<UserResponse>> {
        return userRepository.getCurrentUser()
    }
}