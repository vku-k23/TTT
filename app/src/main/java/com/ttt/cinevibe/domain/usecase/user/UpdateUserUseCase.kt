package com.ttt.cinevibe.domain.usecase.user

import com.ttt.cinevibe.data.remote.models.UserResponse
import com.ttt.cinevibe.data.remote.models.UserProfileRequest
import com.ttt.cinevibe.data.repository.UserRepository
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        firebaseUid: String? = null,
        displayName: String? = null,
        username: String? = null,
        profileImageUrl: String? = null,
        bio: String? = null,
        favoriteGenre: String? = null,
        reviewCount: Int? = null, 
        followersCount: Int? = null,
        followingCount: Int? = null
    ): Flow<Resource<UserResponse>> {
        val profileRequest = UserProfileRequest(
            firebaseUid = firebaseUid,
            displayName = displayName,
            username = username,
            profileImageUrl = profileImageUrl,
            bio = bio,
            favoriteGenre = favoriteGenre,
            reviewCount = reviewCount,
            followersCount = followersCount,
            followingCount = followingCount
        )
        return userRepository.updateUserProfile(profileRequest)
    }
}