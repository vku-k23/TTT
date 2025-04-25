package com.ttt.cinevibe.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.domain.usecase.auth.ForgotPasswordUseCase
import com.ttt.cinevibe.domain.usecase.auth.GetAuthStatusUseCase
import com.ttt.cinevibe.domain.usecase.auth.LoginUseCase
import com.ttt.cinevibe.domain.usecase.auth.LogoutUseCase
import com.ttt.cinevibe.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val getAuthStatusUseCase: GetAuthStatusUseCase
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()
    
    private val _logoutState = MutableStateFlow<AuthState>(AuthState.Idle)
    val logoutState: StateFlow<AuthState> = _logoutState.asStateFlow()
    
    private val _forgotPasswordState = MutableStateFlow<AuthState>(AuthState.Idle)
    val forgotPasswordState: StateFlow<AuthState> = _forgotPasswordState.asStateFlow()

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = AuthState.Loading
            forgotPasswordUseCase(email)
                .catch { e ->
                    if (e is CancellationException) throw e
                    _forgotPasswordState.value = AuthState.Error(e.message ?: "Password reset failed")
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _forgotPasswordState.value = AuthState.Success
                        }
                        is Resource.Error -> {
                            _forgotPasswordState.value = AuthState.Error(result.message ?: "Password reset failed")
                        }
                        is Resource.Loading -> {
                            _forgotPasswordState.value = AuthState.Loading
                        }
                    }
                }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            loginUseCase(email, password)
                .catch { e ->
                    if (e is CancellationException) throw e
                    _loginState.value = AuthState.Error(e.message ?: "Login failed")
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _loginState.value = AuthState.Success
                        }
                        is Resource.Error -> {
                            _loginState.value = AuthState.Error(result.message ?: "Unknown error")
                        }
                        is Resource.Loading -> {
                            _loginState.value = AuthState.Loading
                        }
                    }
                }
        }
    }

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            try {
                _registerState.value = AuthState.Loading
                
                registerUseCase(email, password, username)
                    .catch { e ->
                        if (e is CancellationException) throw e
                        _registerState.value = AuthState.Error(e.message ?: "Registration failed")
                    }
                    .onCompletion { cause ->
                        // If flow completed with exception, it's already handled by catch
                        if (cause == null && _registerState.value is AuthState.Loading) {
                            // If we're still in Loading state at completion, reset to idle
                            _registerState.value = AuthState.Idle
                        }
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _registerState.value = AuthState.Success
                            }
                            is Resource.Error -> {
                                _registerState.value = AuthState.Error(result.message ?: "Unknown error")
                            }
                            is Resource.Loading -> {
                                if (_registerState.value !is AuthState.Success) {
                                    _registerState.value = AuthState.Loading
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _registerState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = AuthState.Loading
            logoutUseCase()
                .catch { e ->
                    if (e is CancellationException) throw e
                    _logoutState.value = AuthState.Error(e.message ?: "Logout failed")
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _logoutState.value = AuthState.Success
                        }
                        is Resource.Error -> {
                            _logoutState.value = AuthState.Error(result.message ?: "Logout failed")
                        }
                        is Resource.Loading -> {
                            _logoutState.value = AuthState.Loading
                        }
                    }
                }
        }
    }

    fun resetAuthStates() {
        _loginState.value = AuthState.Idle
        _registerState.value = AuthState.Idle
        _logoutState.value = AuthState.Idle
        _forgotPasswordState.value = AuthState.Idle
    }

    fun isUserLoggedIn(): Boolean {
        return getAuthStatusUseCase.isUserLoggedIn()
    }

    fun getCurrentUserId(): String? {
        return getAuthStatusUseCase.getCurrentUserId()
    }

    fun getCurrentUserEmail(): String? {
        return getAuthStatusUseCase.getCurrentUserEmail()
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}