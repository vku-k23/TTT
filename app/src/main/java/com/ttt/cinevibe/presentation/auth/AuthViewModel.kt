package com.ttt.cinevibe.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.domain.usecase.auth.ForgotPasswordUseCase
import com.ttt.cinevibe.domain.usecase.auth.GetAuthStatusUseCase
import com.ttt.cinevibe.domain.usecase.auth.LoginUseCase
import com.ttt.cinevibe.domain.usecase.auth.LogoutUseCase
import com.ttt.cinevibe.domain.usecase.auth.RegisterUseCase
import com.ttt.cinevibe.domain.usecase.user.RegisterUserUseCase
import com.ttt.cinevibe.data.local.PendingSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val getAuthStatusUseCase: GetAuthStatusUseCase,
    private val registerUserUseCase: RegisterUserUseCase,
    private val pendingSyncManager: PendingSyncManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()
    
    private val _logoutState = MutableStateFlow<AuthState>(AuthState.Idle)
    val logoutState: StateFlow<AuthState> = _logoutState.asStateFlow()
    
    private val _forgotPasswordState = MutableStateFlow<AuthState>(AuthState.Idle)
    val forgotPasswordState: StateFlow<AuthState> = _forgotPasswordState.asStateFlow()

    // Add states to track Firebase auth and backend registration separately
    private val _firebaseAuthState = MutableStateFlow<FirebaseAuthState>(FirebaseAuthState.Idle)
    val firebaseAuthState: StateFlow<FirebaseAuthState> = _firebaseAuthState

    private val _backendRegState = MutableStateFlow<BackendRegistrationState>(BackendRegistrationState.Idle)
    val backendRegState: StateFlow<BackendRegistrationState> = _backendRegState

    private val _backendSyncAttempted = MutableStateFlow(false)
    val backendSyncAttempted: StateFlow<Boolean> = _backendSyncAttempted

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
                            
                            // Check for pending registration data to sync with backend
                            checkPendingSyncTasks()
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
        android.util.Log.d("RegistrationDebug", "Register function called with: $email, username: $username")
        viewModelScope.launch {
            _registerState.value = AuthState.Loading
            _firebaseAuthState.value = FirebaseAuthState.Loading
            _backendRegState.value = BackendRegistrationState.Idle
            _backendSyncAttempted.value = false
            
            try {
                // Step 1: Register with Firebase first
                var firebaseSuccess = false
                registerUseCase(email, password, username)
                    .catch { e ->
                        if (e is CancellationException) throw e
                        android.util.Log.e("RegistrationDebug", "Firebase registration failed: ${e.message}")
                        _firebaseAuthState.value = FirebaseAuthState.Error(e.message ?: "Firebase registration failed")
                        _registerState.value = AuthState.Error(e.message ?: "Registration failed")
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                android.util.Log.d("RegistrationDebug", "Firebase registration successful")
                                _firebaseAuthState.value = FirebaseAuthState.Success
                                firebaseSuccess = true
                                
                                // Save data for potential sync if backend fails
                                val uid = getAuthStatusUseCase.getCurrentUserId()
                                if (uid != null) {
                                    pendingSyncManager.savePendingRegistration(email, username, uid)
                                }
                            }
                            is Resource.Error -> {
                                android.util.Log.e("RegistrationDebug", "Firebase error: ${result.message}")
                                _firebaseAuthState.value = FirebaseAuthState.Error(result.message ?: "Firebase registration failed")
                                _registerState.value = AuthState.Error(result.message ?: "Registration failed")
                            }
                            is Resource.Loading -> {
                                _firebaseAuthState.value = FirebaseAuthState.Loading
                            }
                        }
                    }
                
                // Step 2: If Firebase registration was successful, proceed with backend registration
                if (firebaseSuccess) {
                    val uid = getAuthStatusUseCase.getCurrentUserId()
                    
                    if (uid != null) {
                        android.util.Log.d("RegistrationDebug", "Starting backend registration with uid: $uid")
                        _backendRegState.value = BackendRegistrationState.Loading
                        
                        // Use a timeout for backend registration to prevent hanging
                        val backendResult = withTimeoutOrNull(15000) { // 15 seconds timeout
                            try {
                                var finalResult: Resource<Any> = Resource.Loading()
                                
                                registerUserUseCase(email, username, uid)
                                    .catch { e ->
                                        if (e is CancellationException) throw e
                                        android.util.Log.e("RegistrationDebug", "Backend registration error: ${e.message}")
                                        finalResult = Resource.Error(e.message ?: "Backend registration failed")
                                    }
                                    .collect { result ->
                                        finalResult = when (result) {
                                            is Resource.Success -> {
                                                android.util.Log.d("RegistrationDebug", "Backend registration successful")
                                                Resource.Success(Unit)
                                            }
                                            is Resource.Error -> {
                                                android.util.Log.e("RegistrationDebug", "Backend registration error: ${result.message}")
                                                Resource.Error(result.message ?: "Backend registration failed")
                                            }
                                            is Resource.Loading -> finalResult
                                        }
                                    }
                                
                                finalResult
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                android.util.Log.e("RegistrationDebug", "Backend registration exception: ${e.message}")
                                Resource.Error(e.message ?: "Backend registration failed")
                            }
                        } ?: Resource.Error("Backend registration timed out")
                        
                        _backendSyncAttempted.value = true
                        
                        // Handle backend result
                        when (backendResult) {
                            is Resource.Success -> {
                                android.util.Log.d("RegistrationDebug", "Backend registration succeeded, clearing pending data")
                                _backendRegState.value = BackendRegistrationState.Success
                                pendingSyncManager.clearPendingRegistration()
                                _registerState.value = AuthState.Success
                            }
                            is Resource.Error -> {
                                android.util.Log.w("RegistrationDebug", "Backend registration failed but Firebase registration succeeded. Continuing with FirebaseAuth only for now: ${backendResult.message}")
                                _backendRegState.value = BackendRegistrationState.Error(backendResult.message ?: "Unknown backend error")
                                
                                // IMPORTANT: Despite backend failure, consider registration successful 
                                // since Firebase auth worked and we've saved data for later sync
                                _registerState.value = AuthState.Success
                            }
                            is Resource.Loading -> {
                                // This shouldn't happen due to the timeout, but just in case
                                android.util.Log.w("RegistrationDebug", "Backend registration still in Loading state after timeout")
                                _backendRegState.value = BackendRegistrationState.Error("Backend registration timed out")
                                _registerState.value = AuthState.Success // Still allow login with Firebase only
                            }
                        }
                    } else {
                        android.util.Log.e("RegistrationDebug", "Firebase UID is null after successful registration")
                        _registerState.value = AuthState.Error("Failed to get user ID after registration")
                    }
                }
                
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                android.util.Log.e("RegistrationDebug", "Uncaught exception in registration: ${e.message}", e)
                _registerState.value = AuthState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    private fun checkPendingSyncTasks() {
        viewModelScope.launch {
            try {
                if (pendingSyncManager.hasPendingRegistration()) {
                    android.util.Log.d("AuthViewModel", "Found pending registration data, attempting to sync with backend")
                    val registrationData = pendingSyncManager.getPendingRegistrationData()
                    
                    if (registrationData != null) {
                        // Try to sync with backend server
                        withTimeoutOrNull(10000) { // 10 second timeout
                            try {
                                registerUserUseCase(
                                    registrationData.email, 
                                    registrationData.displayName, 
                                    registrationData.firebaseUid
                                ).first() // Just take the first emission to complete the flow
                                
                                // If we reach here without exception, sync was successful
                                pendingSyncManager.clearPendingRegistration()
                                android.util.Log.d("AuthViewModel", "Background sync with backend successful")
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                android.util.Log.e("AuthViewModel", "Background sync failed: ${e.message}")
                                // Leave pending data for next attempt
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                android.util.Log.e("AuthViewModel", "Error checking pending sync tasks: ${e.message}")
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

sealed class FirebaseAuthState {
    object Idle : FirebaseAuthState()
    object Loading : FirebaseAuthState()
    object Success : FirebaseAuthState()
    data class Error(val message: String) : FirebaseAuthState()
}

sealed class BackendRegistrationState {
    object Idle : BackendRegistrationState()
    object Loading : BackendRegistrationState()
    object Success : BackendRegistrationState()
    data class Error(val message: String) : BackendRegistrationState()
}