package com.ttt.cinevibe.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.data.remote.models.UserResponse
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.domain.usecase.auth.GetAuthStatusUseCase
import com.ttt.cinevibe.domain.usecase.user.GetCurrentUserUseCase
import com.ttt.cinevibe.domain.usecase.user.SyncUserUseCase
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
    private val syncUserUseCase: SyncUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val getAuthStatusUseCase: GetAuthStatusUseCase
) : ViewModel() {

    private val _userState = MutableStateFlow<UserState<UserResponse>>(UserState.Idle())
    val userState: StateFlow<UserState<UserResponse>> = _userState.asStateFlow()
    
    // Removed _deleteAccountState as backend doesn't support this functionality

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
                
                // Updated to use syncUser instead of registerUser
                syncUserUseCase(email, displayName, firebaseUid)
                    .catch { e ->
                        if (e is CancellationException) throw e
                        _userState.value = UserState.Error(e.message ?: "Sync with backend failed")
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                result.data?.let { userData ->
                                    _userState.value = UserState.Success(userData)
                                } ?: run {
                                    _userState.value = UserState.Error("No user data returned after sync")
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
                _userState.value = UserState.Error(e.message ?: "User sync failed")
            }
        }
    }
    
    // Update user profile info
    fun updateUser(displayName: String? = null, profileImageUrl: String? = null, bio: String? = null, favoriteGenre: String? = null) {
        viewModelScope.launch {
            _userState.value = UserState.Loading()
            
            try {
                updateUserUseCase(null, displayName, profileImageUrl, bio, favoriteGenre)
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
    
    // Deleted account deletion method as backend doesn't support this functionality
    
    fun resetUserStates() {
        _userState.value = UserState.Idle()
        // Removed reset for _deleteAccountState
    }
}

sealed class UserState<out T> {
    class Idle<T> : UserState<T>()
    class Loading<T> : UserState<T>()
    data class Success<T>(val data: T) : UserState<T>()
    data class Error<T>(val message: String) : UserState<T>()
}