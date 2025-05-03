package com.ttt.cinevibe.domain.usecase.user

import com.ttt.cinevibe.data.remote.models.UserProfile
import com.ttt.cinevibe.data.repository.UserRepository
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uid: String): Flow<Resource<UserProfile>> {
        return userRepository.getUserProfile(uid)
    }
}