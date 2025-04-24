package com.ttt.cinevibe.domain.usecase.auth

import com.ttt.cinevibe.data.repository.AuthRepository
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<Resource<Boolean>> {
        return repository.logoutUser()
    }
}