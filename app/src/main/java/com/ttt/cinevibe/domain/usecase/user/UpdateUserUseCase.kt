package com.ttt.cinevibe.domain.usecase.user

import com.ttt.cinevibe.data.remote.models.UserResponse
import com.ttt.cinevibe.data.remote.models.UserUpdateRequest
import com.ttt.cinevibe.data.repository.UserRepository
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        displayName: String? = null,
        profileImageUrl: String? = null,
        bio: String? = null,
        favoriteGenre: String? = null
    ): Flow<Resource<UserResponse>> {
        val updateRequest = UserUpdateRequest(
            displayName = displayName,
            profileImageUrl = profileImageUrl,
            bio = bio,
            favoriteGenre = favoriteGenre
        )
        return userRepository.updateUser(updateRequest)
    }
}