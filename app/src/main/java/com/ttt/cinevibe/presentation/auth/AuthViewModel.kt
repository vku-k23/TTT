package com.ttt.cinevibe.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.domain.usecase.auth.ForgotPasswordUseCase
import com.ttt.cinevibe.domain.usecase.auth.GetAuthStatusUseCase
import com.ttt.cinevibe.domain.usecase.auth.LoginUseCase
import com.ttt.cinevibe.domain.usecase.auth.LogoutUseCase
import com.ttt.cinevibe.domain.usecase.auth.RegisterUseCase
import com.ttt.cinevibe.domain.usecase.user.SyncUserUseCase
import com.ttt.cinevibe.data.local.PendingSyncManager
import com.ttt.cinevibe.data.local.UserPreferences
import com.ttt.cinevibe.data.remote.BackendApiClient
import com.ttt.cinevibe.data.remote.models.UserRequest
import com.ttt.cinevibe.data.remote.models.UserResponse
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
    private val pendingSyncManager: PendingSyncManager,
    private val backendApiClient: BackendApiClient,
    private val userPreferences: UserPreferences,
    private val firebaseAuth: FirebaseAuth
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

    // Setup Firebase auth state listener to detect sign out from other places
    init {
        setupAuthStateListener()
    }

    private fun setupAuthStateListener() {
        firebaseAuth.addAuthStateListener { auth ->
            if (auth.currentUser == null) {
                // User signed out - clear local data
                viewModelScope.launch {
                    android.util.Log.d(TAG, "Auth state changed: signed out, clearing local data")
                    userPreferences.clearUserData()
                }
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = AuthState.Loading
            forgotPasswordUseCase(email)
                .catch { e ->
                    if (e is CancellationException) throw e
                    _forgotPasswordState.value =
                        AuthState.Error(e.message ?: "Password reset failed")
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> _forgotPasswordState.value = AuthState.Success
                        is Resource.Error -> _forgotPasswordState.value =
                            AuthState.Error(result.message ?: "Password reset failed")

                        is Resource.Loading -> _forgotPasswordState.value = AuthState.Loading
                    }
                }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading

            // First, ensure previous user data is cleared to prevent data mixing
            clearUserData()
            
            loginUseCase(email, password)
                .catch { e ->
                    if (e is CancellationException) throw e
                    _loginState.value = AuthState.Error(e.message ?: "Login failed")
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            android.util.Log.d(TAG, "Firebase login successful")

                            // Save email in preferences immediately
                            userPreferences.saveEmail(email)

                            // Check for pending registration data first
                            checkPendingSyncTasks()

                            // Now sync current user with backend
                            _backendSyncState.value = BackendSyncState.Loading
                            try {
                                // First try to get the current user data from backend
                                try {
                                    val userResponse = backendApiClient.getCurrentUser().getOrThrow()
                                    android.util.Log.d(TAG, "Successfully retrieved user data from backend")
                                    
                                    // Save username to preferences if it exists
                                    if (!userResponse.username.isNullOrBlank()) {
                                        android.util.Log.d(TAG, "Saving username from backend: ${userResponse.username}")
                                        userPreferences.saveUsername(userResponse.username)
                                    }
                                    
                                    // Save display name to preferences if it exists
                                    if (!userResponse.displayName.isNullOrBlank()) {
                                        android.util.Log.d(TAG, "Saving display name from backend: ${userResponse.displayName}")
                                        userPreferences.saveDisplayName(userResponse.displayName)
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e(TAG, "Failed to get user data from backend: ${e.message}")
                                }

                                // Then proceed with regular sync
                                syncUserWithBackend()

                                // Always proceed after sync attempt, regardless of result
                                android.util.Log.d(
                                    TAG,
                                    "Login successful and sync with backend attempted"
                                )
                                _loginState.value = AuthState.Success
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                // Log the error but still consider login successful since Firebase auth worked
                                android.util.Log.e(
                                    TAG,
                                    "Error during login sync with backend: ${e.message}"
                                )
                                _loginState.value = AuthState.Success
                            }
                        }

                        is Resource.Error -> _loginState.value =
                            AuthState.Error(result.message ?: "Login failed")

                        is Resource.Loading -> _loginState.value = AuthState.Loading
                    }
                }
        }
    }

    fun register(email: String, password: String,  displayName: String, username: String,) {
        android.util.Log.d(TAG, "Starting registration process for: $email, $displayName, $username")
        viewModelScope.launch {
            _registerState.value = AuthState.Loading
            _firebaseAuthState.value = FirebaseAuthState.Loading

            // First, ensure previous user data is cleared to prevent data mixing
            clearUserData()

            try {
                // Save the user details to preferences upfront
                userPreferences.saveUsername(username)
                userPreferences.saveDisplayName(displayName)
                userPreferences.saveEmail(email)

                // Step 1: Register with Firebase
                var firebaseSuccess = false
                registerUseCase(email, password, displayName, username)
                    .catch { e ->
                        if (e is CancellationException) throw e
                        android.util.Log.e(TAG, "Firebase registration failed: ${e.message}")
                        _firebaseAuthState.value =
                            FirebaseAuthState.Error(e.message ?: "Registration failed")
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
                                    pendingSyncManager.savePendingRegistration(
                                        email,
                                        displayName,
                                        username,
                                        uid
                                    )
                                }
                            }

                            is Resource.Error -> {
                                android.util.Log.e(TAG, "Firebase error: ${result.message}")
                                _firebaseAuthState.value =
                                    FirebaseAuthState.Error(result.message ?: "Registration failed")
                                _registerState.value =
                                    AuthState.Error(result.message ?: "Registration failed")
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
                        val syncResult = backendApiClient.registerWithBackend(
                            displayName = displayName,
                            username = username
                        )
                        syncResult.fold(
                            onSuccess = { userResponse ->
                                android.util.Log.d(TAG, "Backend sync successful")
                                _backendSyncState.value = BackendSyncState.Success
                                _registerState.value = AuthState.Success
                            },
                            onFailure = { error ->
                                android.util.Log.e(TAG, "Backend sync failed: ${error.message}")
                                _backendSyncState.value =
                                    BackendSyncState.Error(error.message ?: "Backend sync failed")
                                // Still mark registration as successful since Firebase auth worked
                                _registerState.value = AuthState.Success
                            }
                        )
                    } catch (e: Exception) {
                        android.util.Log.e(TAG, "Backend sync error: ${e.message}")
                        _backendSyncState.value =
                            BackendSyncState.Error(e.message ?: "Backend sync failed")
                        // Still mark registration as successful since Firebase auth worked
                        _registerState.value = AuthState.Success
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Registration process error: ${e.message}")
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

                val currentUser: UserResponse = backendApiClient.getCurrentUser().getOrThrow()

                if (uid != null && email != null) {
                    android.util.Log.d(TAG, "Syncing user with backend: $uid, email: $email")

                    // Log the username value being used for debugging
                    android.util.Log.d(TAG, "Current username for sync: ${currentUser.username}")

                    // Save user data to preferences
                    if (!currentUser.username.isNullOrEmpty()) {
                        userPreferences.saveUsername(currentUser.username)
                    }
                    if (!currentUser.displayName.isNullOrEmpty()) {
                        userPreferences.saveDisplayName(currentUser.displayName)
                    }
                    userPreferences.saveEmail(email)

                    withTimeoutOrNull(SYNC_TIMEOUT_MS) {
                        try {
                            // Make sure we're using the correct username from the current user response
                            syncUserUseCase(
                                email = email,
                                displayName = currentUser.displayName,
                                username = currentUser.username,
                                firebaseUid = uid
                            )
                                .catch { e ->
                                    if (e is CancellationException) throw e
                                    android.util.Log.e(TAG, "Backend sync error: ${e.message}")
                                    _backendSyncState.value = BackendSyncState.Error(
                                        e.message ?: "Failed to sync with server"
                                    )
                                }
                                .collect { result ->
                                    when (result) {
                                        is Resource.Success -> {
                                            android.util.Log.d(TAG, "Backend sync successful")
                                            _backendSyncState.value = BackendSyncState.Success
                                        }
                                        is Resource.Error -> {
                                            android.util.Log.e(TAG, "Backend sync error: ${result.message}")
                                            _backendSyncState.value = BackendSyncState.Error(
                                                result.message ?: "Failed to sync with server"
                                            )
                                        }
                                        is Resource.Loading -> {
                                            _backendSyncState.value = BackendSyncState.Loading
                                        }
                                    }
                                }
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            android.util.Log.e(TAG, "Backend sync error: ${e.message}")
                            _backendSyncState.value = BackendSyncState.Error(
                                e.message ?: "Failed to sync with server"
                            )
                        }
                    }
                } else {
                    android.util.Log.e(TAG, "Cannot sync user with backend: missing uid or email")
                    _backendSyncState.value = BackendSyncState.Error(
                        "Cannot sync user with backend: missing uid or email"
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                android.util.Log.e(TAG, "Error during sync: ${e.message}")
                _backendSyncState.value = BackendSyncState.Error(
                    e.message ?: "Failed to sync with server"
                )
            }
        }
    }

    /**
     * Check for pending registration or sync tasks
     */
    private suspend fun checkPendingSyncTasks() {
        try {
            val registrationData = pendingSyncManager.getPendingRegistration()
            if (registrationData != null) {
                android.util.Log.d(TAG, "Found pending sync data: $registrationData")
                
                // Check if we're currently logged in with this user
                val currentUid = getAuthStatusUseCase.getCurrentUserId()
                if (currentUid == registrationData.firebaseUid) {
                    android.util.Log.d(TAG, "Attempting to sync pending registration data for current user")
                    try {
                        syncUserUseCase(
                            email = registrationData.email,
                            displayName = registrationData.displayName,
                            username = registrationData.username,
                            firebaseUid = registrationData.firebaseUid
                        ).first()

                        // Sync successful
                        pendingSyncManager.clearPendingRegistration()
                        android.util.Log.d(TAG, "Pending data sync successful")                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        android.util.Log.e(TAG, "Pending data sync failed: ${e.message}")
                        // Keep pending data for next attempt
                    }
                }
            }        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error checking pending sync tasks: ${e.message}")
        }
    }

    /**
     * Clear all user data from preferences
     */
    private suspend fun clearUserData() {
        userPreferences.clearUserData()
    }

    /**
     * Reset all auth states to Idle
     * Used when navigating between auth screens or when an operation completes
     */
    fun resetAuthStates() {
        _loginState.value = AuthState.Idle
        _registerState.value = AuthState.Idle
        _logoutState.value = AuthState.Idle
        _forgotPasswordState.value = AuthState.Idle
        _firebaseAuthState.value = FirebaseAuthState.Idle
        _backendSyncState.value = BackendSyncState.Idle
    }

    /**
     * Logout from Firebase and clear user data
     */
    fun logout() {
        viewModelScope.launch {
            _logoutState.value = AuthState.Loading
            try {
                logoutUseCase()
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _logoutState.value = AuthState.Success
                                // Clear all user data - Firebase AuthStateListener will handle this as well
                                clearUserData()
                            }
                            is Resource.Error -> {
                                _logoutState.value = AuthState.Error(result.message ?: "Logout failed")
                            }
                            is Resource.Loading -> {
                                _logoutState.value = AuthState.Loading
                            }
                        }
                    }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _logoutState.value = AuthState.Error(e.message ?: "Logout failed")
            }
        }
    }

    /**
     * Get current user email from the auth repository
     */
    fun getCurrentUserEmail(): String? {
        return getAuthStatusUseCase.getCurrentUserEmail()
    }
    
    /**
     * Get current user ID from the auth repository
     */
    fun getCurrentUserId(): String? {
        return getAuthStatusUseCase.getCurrentUserId()
    }
    
    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return getAuthStatusUseCase.isUserLoggedIn()
    }
}