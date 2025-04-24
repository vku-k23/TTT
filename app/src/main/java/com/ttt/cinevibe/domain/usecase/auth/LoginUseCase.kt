package com.ttt.cinevibe.domain.usecase.auth

import com.ttt.cinevibe.data.repository.AuthRepository
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(email: String, password: String): Flow<Resource<Boolean>> {
        return repository.loginUser(email, password)
    }
}