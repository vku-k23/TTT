package com.ttt.cinevibe.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.data.remote.models.ApiResponse
import com.ttt.cinevibe.data.remote.models.UserProfile
import com.ttt.cinevibe.data.remote.models.UserResponse
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.domain.usecase.auth.GetAuthStatusUseCase
import com.ttt.cinevibe.domain.usecase.user.DeleteUserUseCase
import com.ttt.cinevibe.domain.usecase.user.GetCurrentUserUseCase
import com.ttt.cinevibe.domain.usecase.user.GetUserProfileUseCase
import com.ttt.cinevibe.domain.usecase.user.RegisterUserUseCase
import com.ttt.cinevibe.domain.usecase.user.UpdateUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel để quản lý dữ liệu người dùng và tương tác với backend API
 */
@HiltViewModel
class ProfileUserViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val registerUserUseCase: RegisterUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getAuthStatusUseCase: GetAuthStatusUseCase
) : ViewModel() {

    private val _userState = MutableStateFlow<UserState<UserResponse>>(UserState.Idle())
    val userState: StateFlow<UserState<UserResponse>> = _userState.asStateFlow()
    
    private val _userProfileState = MutableStateFlow<UserState<UserProfile>>(UserState.Idle())
    val userProfileState: StateFlow<UserState<UserProfile>> = _userProfileState.asStateFlow()
    
    private val _deleteAccountState = MutableStateFlow<UserState<ApiResponse>>(UserState.Idle())
    val deleteAccountState: StateFlow<UserState<ApiResponse>> = _deleteAccountState.asStateFlow()

    // Load current user from backend using Firebase auth token
    fun getCurrentUser() {
        viewModelScope.launch {
            _userState.value = UserState.Loading()
            
            try {
                getCurrentUserUseCase()
                    .catch { e ->
                        if (e is CancellationException) throw e
                        _userState.value = UserState.Error(e.message ?: "Failed to get current user")
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                result.data?.let { userData ->
                                    _userState.value = UserState.Success(userData)
                                } ?: run {
                                    _userState.value = UserState.Error("No user data returned")
                                }
                            }
                            is Resource.Error -> {
                                _userState.value = UserState.Error(result.message ?: "Unknown error")
                            }
                            is Resource.Loading -> {
                                _userState.value = UserState.Loading()
                            }
                        }
                    }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _userState.value = UserState.Error(e.message ?: "Failed to get user data")
            }
        }
    }
    
    // Register user with backend after Firebase authentication
    fun registerUserWithBackend() {
        viewModelScope.launch {
            _userState.value = UserState.Loading()
            
            try {
                // Get Firebase UID and email from auth
                val firebaseUid = getAuthStatusUseCase.getCurrentUserId()
                val email = getAuthStatusUseCase.getCurrentUserEmail()
                
                if (firebaseUid == null || email == null) {
                    _userState.value = UserState.Error("User not authenticated with Firebase")
                    return@launch
                }
                
                // Use email as default display name initially
                val displayName = email.substringBefore('@')
                
                registerUserUseCase(email, displayName, firebaseUid)
                    .catch { e ->
                        if (e is CancellationException) throw e
                        _userState.value = UserState.Error(e.message ?: "Registration with backend failed")
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                result.data?.let { userData ->
                                    _userState.value = UserState.Success(userData)
                                } ?: run {
                                    _userState.value = UserState.Error("No user data returned after registration")
                                }
                            }
                            is Resource.Error -> {
                                _userState.value = UserState.Error(result.message ?: "Unknown error")
                            }
                            is Resource.Loading -> {
                                _userState.value = UserState.Loading()
                            }
                        }
                    }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _userState.value = UserState.Error(e.message ?: "Registration failed")
            }
        }
    }
    
    // Update user profile info
    fun updateUser(displayName: String? = null, profileImageUrl: String? = null, bio: String? = null, favoriteGenre: String? = null) {
        viewModelScope.launch {
            _userState.value = UserState.Loading()
            
            try {
                updateUserUseCase(displayName, profileImageUrl, bio, favoriteGenre)
                    .catch { e ->
                        if (e is CancellationException) throw e
                        _userState.value = UserState.Error(e.message ?: "Failed to update profile")
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                result.data?.let { userData ->
                                    _userState.value = UserState.Success(userData)
                                } ?: run {
                                    _userState.value = UserState.Error("No user data returned after update")
                                }
                            }
                            is Resource.Error -> {
                                _userState.value = UserState.Error(result.message ?: "Unknown error")
                            }
                            is Resource.Loading -> {
                                _userState.value = UserState.Loading()
                            }
                        }
                    }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _userState.value = UserState.Error(e.message ?: "Profile update failed")
            }
        }
    }
    
    // Delete user account
    fun deleteAccount() {
        viewModelScope.launch {
            _deleteAccountState.value = UserState.Loading()
            
            try {
                deleteUserUseCase()
                    .catch { e ->
                        if (e is CancellationException) throw e
                        _deleteAccountState.value = UserState.Error(e.message ?: "Failed to delete account")
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                result.data?.let { response ->
                                    _deleteAccountState.value = UserState.Success(response)
                                } ?: run {
                                    _deleteAccountState.value = UserState.Error("No response data returned after account deletion")
                                }
                            }
                            is Resource.Error -> {
                                _deleteAccountState.value = UserState.Error(result.message ?: "Unknown error")
                            }
                            is Resource.Loading -> {
                                _deleteAccountState.value = UserState.Loading()
                            }
                        }
                    }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _deleteAccountState.value = UserState.Error(e.message ?: "Account deletion failed")
            }
        }
    }
    
    // Get a public user profile
    fun getUserProfile(uid: String) {
        viewModelScope.launch {
            _userProfileState.value = UserState.Loading()
            
            try {
                getUserProfileUseCase(uid)
                    .catch { e ->
                        if (e is CancellationException) throw e
                        _userProfileState.value = UserState.Error(e.message ?: "Failed to get profile")
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                result.data?.let { profile ->
                                    _userProfileState.value = UserState.Success(profile)
                                } ?: run {
                                    _userProfileState.value = UserState.Error("No profile data returned")
                                }
                            }
                            is Resource.Error -> {
                                _userProfileState.value = UserState.Error(result.message ?: "Unknown error")
                            }
                            is Resource.Loading -> {
                                _userProfileState.value = UserState.Loading()
                            }
                        }
                    }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _userProfileState.value = UserState.Error(e.message ?: "Failed to load profile")
            }
        }
    }
    
    fun resetUserStates() {
        _userState.value = UserState.Idle()
        _userProfileState.value = UserState.Idle()
        _deleteAccountState.value = UserState.Idle()
    }
}

sealed class UserState<out T> {
    class Idle<T> : UserState<T>()
    class Loading<T> : UserState<T>()
    data class Success<T>(val data: T) : UserState<T>()
    data class Error<T>(val message: String) : UserState<T>()
}