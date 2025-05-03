package com.ttt.cinevibe.domain.usecase.user

import com.ttt.cinevibe.data.remote.models.UserResponse
import com.ttt.cinevibe.data.remote.models.UserProfileRequest
import com.ttt.cinevibe.data.repository.UserRepository
import com.ttt.cinevibe.domain.model.Resource
import com.google.firebase.firestore.firestoreSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        firebaseUid: String? = null,
        displayName: String? = null,
        profileImageUrl: String? = null,
        bio: String? = null,
        favoriteGenre: String? = null,
        reviewCount: String? = null, 
        followersCount: String? = null,
        followingCount: String? = null
    ): Flow<Resource<UserResponse>> {
        val updateRequest = UserProfileRequest(
            firebaseUid = firebaseUid,
            displayName = displayName,
            profileImageUrl = profileImageUrl,
            bio = bio,
            favoriteGenre = favoriteGenre,
            reviewCount = reviewCount,
            followersCount = followersCount,
            followingCount = followingCount
        )
        return userRepository.updateUser(updateRequest)
    }
}