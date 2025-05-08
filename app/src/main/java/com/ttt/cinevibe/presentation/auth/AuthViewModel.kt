package com.ttt.cinevibe.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.domain.usecase.auth.ForgotPasswordUseCase
import com.ttt.cinevibe.domain.usecase.auth.GetAuthStatusUseCase
import com.ttt.cinevibe.domain.usecase.auth.LoginUseCase
import com.ttt.cinevibe.domain.usecase.auth.LogoutUseCase
import com.ttt.cinevibe.domain.usecase.auth.RegisterUseCase
import com.ttt.cinevibe.domain.usecase.user.SyncUserUseCase
import com.ttt.cinevibe.data.local.PendingSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

private const val TAG = "AuthViewModel"
private const val SYNC_TIMEOUT_MS = 10000L // 10 seconds

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val getAuthStatusUseCase: GetAuthStatusUseCase,
    private val syncUserUseCase: SyncUserUseCase,
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

    // Consolidated states for Firebase auth and backend sync
    private val _firebaseAuthState = MutableStateFlow<FirebaseAuthState>(FirebaseAuthState.Idle)
    val firebaseAuthState: StateFlow<FirebaseAuthState> = _firebaseAuthState

    private val _backendSyncState = MutableStateFlow<BackendSyncState>(BackendSyncState.Idle)
    val backendSyncState: StateFlow<BackendSyncState> = _backendSyncState

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
                        is Resource.Success -> _forgotPasswordState.value = AuthState.Success
                        is Resource.Error -> _forgotPasswordState.value = AuthState.Error(result.message ?: "Password reset failed")
                        is Resource.Loading -> _forgotPasswordState.value = AuthState.Loading
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
                            android.util.Log.d(TAG, "Firebase login successful")
                            
                            // Check for pending registration data first
                            checkPendingSyncTasks()
                            
                            // Now sync current user with backend
                            _backendSyncState.value = BackendSyncState.Loading
                            try {
                                // Sync with backend (we don't care about result here, just log it)
                                syncUserWithBackend()
                                
                                // Always proceed after sync attempt, regardless of result
                                android.util.Log.d(TAG, "Login successful and sync with backend attempted")
                                _loginState.value = AuthState.Success
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                // Log the error but still consider login successful since Firebase auth worked
                                android.util.Log.e(TAG, "Error during login sync with backend: ${e.message}")
                                _loginState.value = AuthState.Success
                            }
                        }
                        is Resource.Error -> _loginState.value = AuthState.Error(result.message ?: "Login failed")
                        is Resource.Loading -> _loginState.value = AuthState.Loading
                    }
                }
        }
    }

    fun register(email: String, password: String, username: String) {
        android.util.Log.d(TAG, "Starting registration process for: $email")
        viewModelScope.launch {
            _registerState.value = AuthState.Loading
            _firebaseAuthState.value = FirebaseAuthState.Loading
            
            try {
                // Step 1: Register with Firebase
                var firebaseSuccess = false
                registerUseCase(email, password, username)
                    .catch { e ->
                        if (e is CancellationException) throw e
                        android.util.Log.e(TAG, "Firebase registration failed: ${e.message}")
                        _firebaseAuthState.value = FirebaseAuthState.Error(e.message ?: "Registration failed")
                        _registerState.value = AuthState.Error(e.message ?: "Registration failed")
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                android.util.Log.d(TAG, "Firebase registration successful")
                                _firebaseAuthState.value = FirebaseAuthState.Success
                                firebaseSuccess = true
                                
                                // Save data for potential sync if backend fails
                                val uid = getAuthStatusUseCase.getCurrentUserId()
                                if (uid != null) {
                                    pendingSyncManager.savePendingRegistration(email, username, uid)
                                }
                            }
                            is Resource.Error -> {
                                android.util.Log.e(TAG, "Firebase error: ${result.message}")
                                _firebaseAuthState.value = FirebaseAuthState.Error(result.message ?: "Registration failed")
                                _registerState.value = AuthState.Error(result.message ?: "Registration failed")
                            }
                            is Resource.Loading -> {
                                _firebaseAuthState.value = FirebaseAuthState.Loading
                            }
                        }
                    }
                
                // Step 2: If Firebase registration was successful, sync user with backend
                if (firebaseSuccess) {
                    _backendSyncState.value = BackendSyncState.Loading
                    try {
                        // We're deliberately not waiting for backend sync to complete
                        // Just kick off the sync process
                        syncUserWithBackend()
                        
                        // Always consider registration successful if Firebase auth worked
                        android.util.Log.d(TAG, "Registration successful and backend sync initiated")
                        _registerState.value = AuthState.Success
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        // Log the error but still consider registration successful since Firebase auth worked
                        android.util.Log.e(TAG, "Error during registration sync with backend: ${e.message}")
                        _registerState.value = AuthState.Success
                    }
                }
                
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                android.util.Log.e(TAG, "Uncaught exception in registration: ${e.message}", e)
                _registerState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    /**
     * Unified method to sync the current user with backend
     * Used for both login and registration flows
     * 
     * This method returns immediately but handles the sync in a coroutine
     */
    private fun syncUserWithBackend() {
        viewModelScope.launch {
            try {
                _backendSyncState.value = BackendSyncState.Loading
                
                val uid = getAuthStatusUseCase.getCurrentUserId()
                val email = getAuthStatusUseCase.getCurrentUserEmail()
                
                if (uid != null && email != null) {
                    android.util.Log.d(TAG, "Syncing user with backend: $uid, email: $email")
                    
                    // Use email prefix as default username if needed
                    val username = email.substringBefore('@')
                    
                    withTimeoutOrNull(SYNC_TIMEOUT_MS) {
                        try {
                            syncUserUseCase(email, username, uid)
                                .catch { e ->
                                    if (e is CancellationException) throw e
                                    android.util.Log.e(TAG, "Backend sync error: ${e.message}")
                                    _backendSyncState.value = BackendSyncState.Error(e.message ?: "Failed to sync with server")
                                }
                                .collect { result ->
                                    when (result) {
                                        is Resource.Success -> {
                                            android.util.Log.d(TAG, "Backend sync successful")
                                            _backendSyncState.value = BackendSyncState.Success
                                            pendingSyncManager.clearPendingRegistration()
                                        }
                                        is Resource.Error -> {
                                            android.util.Log.e(TAG, "Backend sync error: ${result.message}")
                                            _backendSyncState.value = BackendSyncState.Error(result.message ?: "Sync failed")
                                        }
                                        is Resource.Loading -> {} // Stay in loading state
                                    }
                                }
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            android.util.Log.e(TAG, "Backend sync exception: ${e.message}")
                            _backendSyncState.value = BackendSyncState.Error(e.message ?: "Sync failed")
                        }
                    } ?: run {
                        // Timeout occurred
                        android.util.Log.w(TAG, "Backend sync timed out")
                        _backendSyncState.value = BackendSyncState.Error("Sync timed out")
                    }
                } else {
                    android.util.Log.e(TAG, "Missing user data for sync: uid=$uid, email=$email")
                    _backendSyncState.value = BackendSyncState.Error("Missing user data")
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                android.util.Log.e(TAG, "General error during sync: ${e.message}")
                _backendSyncState.value = BackendSyncState.Error(e.message ?: "Sync failed")
            }
        }
    }

    private fun checkPendingSyncTasks() {
        viewModelScope.launch {
            try {
                if (pendingSyncManager.hasPendingRegistration()) {
                    android.util.Log.d(TAG, "Found pending registration data")
                    val registrationData = pendingSyncManager.getPendingRegistrationData()
                    
                    if (registrationData != null) {
                        // Try to sync with backend
                        withTimeoutOrNull(SYNC_TIMEOUT_MS) {
                            try {
                                syncUserUseCase(
                                    registrationData.email, 
                                    registrationData.displayName, 
                                    registrationData.firebaseUid
                                ).first()
                                
                                // Sync successful
                                pendingSyncManager.clearPendingRegistration()
                                android.util.Log.d(TAG, "Pending data sync successful")
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                android.util.Log.e(TAG, "Pending data sync failed: ${e.message}")
                                // Keep pending data for next attempt
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                android.util.Log.e(TAG, "Error checking pending sync tasks: ${e.message}")
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
                        is Resource.Success -> _logoutState.value = AuthState.Success
                        is Resource.Error -> _logoutState.value = AuthState.Error(result.message ?: "Logout failed")
                        is Resource.Loading -> _logoutState.value = AuthState.Loading
                    }
                }
        }
    }

    fun resetAuthStates() {
        _loginState.value = AuthState.Idle
        _registerState.value = AuthState.Idle
        _logoutState.value = AuthState.Idle
        _forgotPasswordState.value = AuthState.Idle
        _firebaseAuthState.value = FirebaseAuthState.Idle
        _backendSyncState.value = BackendSyncState.Idle
    }

    fun isUserLoggedIn(): Boolean = getAuthStatusUseCase.isUserLoggedIn()
    fun getCurrentUserId(): String? = getAuthStatusUseCase.getCurrentUserId()
    fun getCurrentUserEmail(): String? = getAuthStatusUseCase.getCurrentUserEmail()
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

// Consolidated backend sync state (replaces separate states for registration and login)
sealed class BackendSyncState {
    object Idle : BackendSyncState()
    object Loading : BackendSyncState()
    object Success : BackendSyncState()
    data class Error(val message: String) : BackendSyncState()
}