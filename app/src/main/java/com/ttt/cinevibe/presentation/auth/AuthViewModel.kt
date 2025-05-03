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
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val getAuthStatusUseCase: GetAuthStatusUseCase,
    private val registerUserUseCase: RegisterUserUseCase,
    private val pendingSyncManager: PendingSyncManager // Thêm PendingSyncManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()
    
    private val _logoutState = MutableStateFlow<AuthState>(AuthState.Idle)
    val logoutState: StateFlow<AuthState> = _logoutState.asStateFlow()
    
    private val _forgotPasswordState = MutableStateFlow<AuthState>(AuthState.Idle)
    val forgotPasswordState: StateFlow<AuthState> = _forgotPasswordState.asStateFlow()

    // Thêm trạng thái theo dõi firebase auth
    private val _firebaseAuthState = MutableStateFlow<FirebaseAuthState>(FirebaseAuthState.Idle)
    val firebaseAuthState: StateFlow<FirebaseAuthState> = _firebaseAuthState

    // Thêm trạng thái theo dõi backend registration
    private val _backendRegState = MutableStateFlow<BackendRegistrationState>(BackendRegistrationState.Idle)
    val backendRegState: StateFlow<BackendRegistrationState> = _backendRegState

    // Biến để theo dõi xem đã hoàn thành đồng bộ lên backend chưa
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
                            
                            // Kiểm tra và xử lý các đồng bộ đang chờ khi đăng nhập thành công
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
        android.util.Log.d("RegistrationDebug", "Register function started.")
        viewModelScope.launch {
            android.util.Log.d("RegistrationDebug", "Setting state to Loading.")
            _registerState.value = AuthState.Loading
            _firebaseAuthState.value = FirebaseAuthState.Loading // Track Firebase Auth separately if needed
            _backendRegState.value = BackendRegistrationState.Idle // Track Backend Reg separately if needed
            _backendSyncAttempted.value = false

            try {
                // Step 1: Firebase Authentication
                registerUseCase(email, password, username)
                    .catch { e ->
                        if (e is CancellationException) throw e
                        _firebaseAuthState.value = FirebaseAuthState.Error(e.message ?: "Firebase registration failed")
                        _registerState.value = AuthState.Error(e.message ?: "Registration failed") // Update main state on Firebase error
                    }
                    .collect { firebaseResult ->
                         android.util.Log.d("RegistrationDebug", "Firebase Result: $firebaseResult")
                        when (firebaseResult) {
                            is Resource.Success -> {
                                android.util.Log.d("RegistrationDebug", "Firebase Success. Calling registerWithBackend...")
                                _firebaseAuthState.value = FirebaseAuthState.Success
                                android.util.Log.d("AuthViewModel", "Firebase registration successful.")

                                // Save data for potential later sync
                                saveRegistrationData(email, username)

                                // Step 2: Backend Registration (await completion)
                                val backendResult = registerWithBackend(email, username) // Call suspend fun
                                android.util.Log.d("RegistrationDebug", "Backend Result: $backendResult")

                                // Step 3: Update final state based on both results
                                android.util.Log.d("RegistrationDebug", "Setting final state based on backend result: $backendResult")
                                when (backendResult) {
                                    is Resource.Success -> {
                                        _registerState.value = AuthState.Success // Final success only after backend success
                                        android.util.Log.d("AuthViewModel", "Backend registration successful, setting final state to Success.")
                                    }
                                    is Resource.Error -> {
                                        _registerState.value = AuthState.Error(backendResult.message ?: "Backend registration failed")
                                        android.util.Log.e("AuthViewModel", "Backend registration failed, setting final state to Error: ${backendResult.message}")
                                        // Note: Firebase Auth succeeded, but backend failed. User exists in Firebase.
                                        // Pending sync should handle retries.
                                    }
                                    is Resource.Loading -> {
                                        // This case shouldn't happen if registerWithBackend awaits completion
                                        _registerState.value = AuthState.Loading
                                    }
                                }
                            }
                            is Resource.Error -> {
                                android.util.Log.e("RegistrationDebug", "Firebase Error: ${firebaseResult.message}. Setting state to Error.")
                                _firebaseAuthState.value = FirebaseAuthState.Error(firebaseResult.message ?: "Firebase registration failed")
                                _registerState.value = AuthState.Error(firebaseResult.message ?: "Registration failed") // Update main state
                                android.util.Log.e("AuthViewModel", "Firebase registration failed: ${firebaseResult.message}")
                            }
                            is Resource.Loading -> {
                                _firebaseAuthState.value = FirebaseAuthState.Loading
                                // Keep _registerState as Loading
                            }
                        }
                    }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                android.util.Log.e("RegistrationDebug", "Caught exception in register: ${e.message}", e)
                android.util.Log.e("AuthViewModel", "Overall registration error: ${e.message}", e)
                _registerState.value = AuthState.Error(e.message ?: "An unexpected error occurred during registration")
            } finally {
                 android.util.Log.d("RegistrationDebug", "Register function finally block executing. Current state: ${_registerState.value}")
                 // Ensure loading stops if coroutine is cancelled or finishes unexpectedly,
                 // unless it's already in a final Success or Error state.
                if (_registerState.value is AuthState.Loading) {
                     _registerState.value = AuthState.Idle // Or Error if appropriate based on context
                     android.util.Log.w("AuthViewModel", "Registration coroutine finished while still in Loading state, resetting to Idle.")
                }
            }
        }
    }

    // Lưu dữ liệu đăng ký tạm thời vào SharedPreferences để tái đồng bộ sau nếu cần
    private fun saveRegistrationData(email: String, displayName: String) {
        viewModelScope.launch { // Can remain a separate launch for saving data
            try {
                val firebaseUid = getAuthStatusUseCase.getCurrentUserId()
                if (firebaseUid != null) {
                    pendingSyncManager.savePendingRegistration(email, displayName, firebaseUid)
                    android.util.Log.d("AuthViewModel", "Saved registration data for future sync: $email, $displayName, $firebaseUid")
                } else {
                     android.util.Log.w("AuthViewModel", "Could not save registration data: Firebase UID is null after successful auth.")
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error saving registration data: ${e.message}")
            }
        }
    }

    // Refactored to be a suspend function returning the result
    private suspend fun registerWithBackend(email: String, displayName: String): Resource<Unit> {
         android.util.Log.d("RegistrationDebug", "registerWithBackend started.")
         android.util.Log.d("RegistrationDebug", "Setting backend state to Loading.")
         _backendRegState.value = BackendRegistrationState.Loading
         _backendSyncAttempted.value = false // Reset attempt flag for this specific call
         var finalResult: Resource<Unit> = Resource.Loading() // Default to loading

         try {
             val firebaseUid = getAuthStatusUseCase.getCurrentUserId()
             if (firebaseUid == null) {
                 _backendRegState.value = BackendRegistrationState.Error("Firebase UID not available for backend registration")
                 _backendSyncAttempted.value = true // Mark as attempted (though failed early)
                 return Resource.Error("Firebase UID not available")
             }

             android.util.Log.d("AuthViewModel", "Starting backend registration call: $email, $displayName, $firebaseUid")

             // Use timeout for the backend call directly
             finalResult = kotlinx.coroutines.withTimeoutOrNull(30000L) {
                 var flowResult: Resource<Unit> = Resource.Loading() // Track inner flow result
                 try {
                     android.util.Log.d("RegistrationDebug", "Calling backend API...")
                     registerUserUseCase(email, displayName, firebaseUid)
                         .catch { e ->
                             if (e is CancellationException) throw e
                             android.util.Log.e("AuthViewModel", "Backend registration flow error: ${e.message}")
                             flowResult = Resource.Error(e.message ?: "Backend registration failed")
                         }
                         .collect { result ->
                              android.util.Log.d("RegistrationDebug", "Backend API Flow Result: $result")
                             flowResult = when (result) {
                                 is Resource.Success -> Resource.Success(Unit)
                                 is Resource.Error -> Resource.Error(result.message ?: "Backend registration failed")
                                 is Resource.Loading -> Resource.Loading()
                             }
                         }
                 } catch (e: Exception) {
                     if (e is CancellationException) throw e
                     android.util.Log.e("AuthViewModel", "Backend registration use case exception: ${e.message}")
                     flowResult = Resource.Error(e.message ?: "Backend registration failed")
                 }
                 flowResult // Return the collected result from the flow
             } ?: Resource.Error("Backend registration timed out") // Result if timeout occurs
             android.util.Log.d("RegistrationDebug", "Backend API call finished or timed out. Final Result: $finalResult")

             // Update backend-specific state
             when (finalResult) {
                 is Resource.Success -> {
                     _backendRegState.value = BackendRegistrationState.Success
                     android.util.Log.d("AuthViewModel", "Backend registration call successful.")
                     // Clear pending sync on success
                     pendingSyncManager.clearPendingRegistration()
                 }
                 is Resource.Error -> {
                     _backendRegState.value = BackendRegistrationState.Error(finalResult.message ?: "Unknown backend error")
                     android.util.Log.e("AuthViewModel", "Backend registration call failed: ${finalResult.message}")
                     // Ensure data is saved for retry on error
                     scheduleBackendSync(email, displayName, firebaseUid)
                 }
                 is Resource.Loading -> {
                      // Should ideally not stay in Loading here due to collect finishing or timeout
                      _backendRegState.value = BackendRegistrationState.Error("Backend registration ended unexpectedly in Loading state.")
                      android.util.Log.w("AuthViewModel", "Backend registration finished unexpectedly in Loading state.")
                      scheduleBackendSync(email, displayName, firebaseUid) // Save for retry just in case
                 }
             }

         } catch (e: Exception) {
             if (e is CancellationException) throw e
             android.util.Log.e("RegistrationDebug", "Caught exception in registerWithBackend: ${e.message}", e)
             _backendRegState.value = BackendRegistrationState.Error(e.message ?: "Backend registration outer exception")
             android.util.Log.e("AuthViewModel", "Backend registration outer exception: ${e.message}")
             finalResult = Resource.Error(e.message ?: "Backend registration failed")
             // Attempt to save for retry even with outer exception
             val uid = getAuthStatusUseCase.getCurrentUserId() // Try getting UID again if possible
             if(uid != null) scheduleBackendSync(email, displayName, uid)

         } finally {
             _backendSyncAttempted.value = true // Mark sync as attempted regardless of outcome
         }
         android.util.Log.d("RegistrationDebug", "Returning from registerWithBackend. Result: $finalResult")
         return finalResult // Return the final result of the backend operation
    }

    // Lên lịch đồng bộ lại với backend nếu thất bại (Simplified: called internally by registerWithBackend on error)
    private fun scheduleBackendSync(email: String, displayName: String, firebaseUid: String) {
        viewModelScope.launch {
            if (_backendRegState.value !is BackendRegistrationState.Success) {
                // Lưu thông tin vào PendingSyncManager để đồng bộ lại sau
                pendingSyncManager.savePendingRegistration(email, displayName, firebaseUid)
            } else {
                // Xóa bất kỳ đồng bộ đang chờ nếu đã đồng bộ thành công
                pendingSyncManager.clearPendingRegistration()
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

    /**
     * Kiểm tra và thực hiện các đồng bộ đang chờ
     */
    private fun checkPendingSyncTasks() {
        viewModelScope.launch {
            if (pendingSyncManager.hasPendingRegistration()) {
                val registrationData = pendingSyncManager.getPendingRegistrationData()
                if (registrationData != null) {
                    android.util.Log.d("AuthViewModel", "Found pending registration, attempting to sync with backend")
                    
                    // Thực hiện đồng bộ lại với backend
                    trySyncWithBackend(
                        registrationData.email,
                        registrationData.displayName,
                        registrationData.firebaseUid
                    )
                }
            }
        }
    }
    
    /**
     * Thử đồng bộ lại với backend
     */
    private fun trySyncWithBackend(email: String, displayName: String, firebaseUid: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("AuthViewModel", "Attempting to sync user with backend: $email, $firebaseUid")
                
                // Gọi API đăng ký với backend với timeout 10 giây
                kotlinx.coroutines.withTimeoutOrNull(10000L) {
                    registerUserUseCase(email, displayName, firebaseUid)
                        .catch { e ->
                            if (e is CancellationException) throw e
                            android.util.Log.e("AuthViewModel", "Backend sync failed: ${e.message}")
                        }
                        .collect { result ->
                            when (result) {
                                is Resource.Success -> {
                                    android.util.Log.d("AuthViewModel", "Backend sync successful")
                                    // Xóa bất kỳ đồng bộ đang chờ sau khi thành công
                                    pendingSyncManager.clearPendingRegistration()
                                }
                                is Resource.Error -> {
                                    android.util.Log.e("AuthViewModel", "Backend sync error: ${result.message}")
                                }
                                is Resource.Loading -> {
                                    // Giữ trạng thái loading
                                }
                            }
                        }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                android.util.Log.e("AuthViewModel", "Backend sync exception: ${e.message}")
            }
        }
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