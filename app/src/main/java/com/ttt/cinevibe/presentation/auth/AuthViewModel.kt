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
        viewModelScope.launch {
            try {
                _registerState.value = AuthState.Loading
                _firebaseAuthState.value = FirebaseAuthState.Loading
                _backendRegState.value = BackendRegistrationState.Idle
                _backendSyncAttempted.value = false
                
                registerUseCase(email, password, username)
                    .catch { e ->
                        if (e is CancellationException) throw e
                        _firebaseAuthState.value = FirebaseAuthState.Error(e.message ?: "Firebase registration failed")
                        _registerState.value = AuthState.Error(e.message ?: "Registration failed")
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                // Đánh dấu Firebase Auth thành công
                                _firebaseAuthState.value = FirebaseAuthState.Success
                                
                                // Lưu dữ liệu đăng ký tạm thời để tái đồng bộ nếu cần
                                saveRegistrationData(email, username)
                                
                                // Kích hoạt đăng ký backend nhưng không chờ đợi
                                registerWithBackend(email, username)
                                
                                // Cho phép người dùng tiếp tục luồng sử dụng
                                _registerState.value = AuthState.Success
                            }
                            is Resource.Error -> {
                                _firebaseAuthState.value = FirebaseAuthState.Error(result.message ?: "Firebase registration failed")
                                _registerState.value = AuthState.Error(result.message ?: "Registration failed")
                            }
                            is Resource.Loading -> {
                                _firebaseAuthState.value = FirebaseAuthState.Loading
                                if (_registerState.value !is AuthState.Success) {
                                    _registerState.value = AuthState.Loading
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _firebaseAuthState.value = FirebaseAuthState.Error(e.message ?: "Firebase registration failed")
                _registerState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }
    
    // Lưu dữ liệu đăng ký tạm thời vào SharedPreferences để tái đồng bộ sau nếu cần
    private fun saveRegistrationData(email: String, displayName: String) {
        viewModelScope.launch {
            try {
                // Lưu dữ liệu Firebase UID để có thể đồng bộ lại sau
                val firebaseUid = getAuthStatusUseCase.getCurrentUserId()
                if (firebaseUid != null) {
                    pendingSyncManager.savePendingRegistration(email, displayName, firebaseUid)
                    android.util.Log.d("AuthViewModel", "Saved registration data for future sync: $email, $displayName, $firebaseUid")
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error saving registration data: ${e.message}")
            }
        }
    }

    // Thêm phương thức để đăng ký với backend
    private fun registerWithBackend(email: String, displayName: String) {
        viewModelScope.launch {
            try {
                _backendRegState.value = BackendRegistrationState.Loading
                
                // Lấy Firebase UID - chỉ gọi sau khi Firebase auth thành công
                val firebaseUid = getAuthStatusUseCase.getCurrentUserId()
                
                if (firebaseUid == null) {
                    _backendRegState.value = BackendRegistrationState.Error("Firebase UID not available")
                    return@launch
                }
                
                // Gọi API đăng ký với backend với timeout 5 giây
                kotlinx.coroutines.withTimeoutOrNull(5000L) {
                    try {
                        registerUserUseCase(email, displayName, firebaseUid)
                            .catch { e ->
                                if (e is CancellationException) throw e
                                _backendRegState.value = BackendRegistrationState.Error(e.message ?: "Backend registration failed")
                                android.util.Log.e("AuthViewModel", "Backend registration failed: ${e.message}")
                            }
                            .collect { result ->
                                when (result) {
                                    is Resource.Success -> {
                                        _backendRegState.value = BackendRegistrationState.Success
                                        android.util.Log.d("AuthViewModel", "Backend registration successful")
                                    }
                                    is Resource.Error -> {
                                        _backendRegState.value = BackendRegistrationState.Error(result.message ?: "Backend registration failed")
                                        android.util.Log.e("AuthViewModel", "Backend registration error: ${result.message}")
                                    }
                                    is Resource.Loading -> {
                                        // Giữ trạng thái loading
                                    }
                                }
                            }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        _backendRegState.value = BackendRegistrationState.Error(e.message ?: "Backend registration failed")
                        android.util.Log.e("AuthViewModel", "Backend registration exception: ${e.message}")
                        null
                    }
                } ?: run {
                    // Timeout xảy ra
                    _backendRegState.value = BackendRegistrationState.Error("Backend registration timed out")
                    android.util.Log.e("AuthViewModel", "Backend registration timed out after 5 seconds")
                }
                
                // Đánh dấu đã thực hiện đồng bộ
                _backendSyncAttempted.value = true
                
                // Lưu thông tin để tái đồng bộ sau nếu cần
                scheduleBackendSync(email, displayName, firebaseUid)
                
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _backendRegState.value = BackendRegistrationState.Error(e.message ?: "Backend registration failed")
                _backendSyncAttempted.value = true
                android.util.Log.e("AuthViewModel", "Backend registration outer exception: ${e.message}")
            }
        }
    }
    
    // Lên lịch đồng bộ lại với backend nếu thất bại
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