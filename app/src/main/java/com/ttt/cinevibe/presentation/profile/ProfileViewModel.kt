package com.ttt.cinevibe.presentation.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.data.manager.LanguageManager
import com.ttt.cinevibe.data.remote.CloudinaryService
import com.ttt.cinevibe.data.remote.models.UserProfileRequest
import com.ttt.cinevibe.data.remote.models.UserResponse
import com.ttt.cinevibe.data.repository.UserRepository
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.domain.usecase.auth.GetAuthStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val languageManager: LanguageManager,
    private val userRepository: UserRepository,
    private val getAuthStatusUseCase: GetAuthStatusUseCase,
    private val cloudinaryService: CloudinaryService
) : ViewModel() {
    
    // Settings state flows
    private val _notificationEnabled = MutableStateFlow(true)
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled
    
    private val _downloadOnWifiOnly = MutableStateFlow(true)
    val downloadOnWifiOnly: StateFlow<Boolean> = _downloadOnWifiOnly
    
    private val _enableAutoplay = MutableStateFlow(true)
    val enableAutoplay: StateFlow<Boolean> = _enableAutoplay
    
    private val _enableSubtitles = MutableStateFlow(false)
    val enableSubtitles: StateFlow<Boolean> = _enableSubtitles
    
    // Language settings
    val selectedLanguage = languageManager.getAppLanguage()
        .map { locale -> languageManager.getLanguageNameFromCode(locale.language) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            "English" // Default value
        )
    
    val currentLocale = languageManager.getAppLanguage()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Locale.getDefault()
        )
    
    // User profile states
    private val _userProfileState = MutableStateFlow<Resource<UserResponse>>(Resource.Loading())
    val userProfileState: StateFlow<Resource<UserResponse>> = _userProfileState
    
    // Using null as initial state instead of non-existent Idle state
    private val _updateProfileState = MutableStateFlow<Resource<UserResponse>?>(null)
    val updateProfileState: StateFlow<Resource<UserResponse>?> = _updateProfileState
    
    // Avatar upload state
    private val _avatarUploadState = MutableStateFlow<Resource<String>?>(null)
    val avatarUploadState: StateFlow<Resource<String>?> = _avatarUploadState
    
    // Initialize by fetching current user data
    init {
        fetchCurrentUser()
    }
    
    fun fetchCurrentUser(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _userProfileState.value = Resource.Loading()
                android.util.Log.d("ProfileViewModel", "Fetching current user data with forceRefresh=$forceRefresh")
                userRepository.getCurrentUser(forceRefresh).collect { result ->
                    _userProfileState.value = result
                    if (result is Resource.Success) {
                        android.util.Log.d("ProfileViewModel", "User data fetched successfully: ${result.data}")
                    } else if (result is Resource.Error) {
                        android.util.Log.e("ProfileViewModel", "Error fetching user data: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                android.util.Log.e("ProfileViewModel", "Exception fetching user data", e)
                _userProfileState.value = Resource.Error(e.message ?: "Failed to load user profile")
            }
        }
    }
    
    // Force refresh user data from server by invalidating cache
    fun refreshUserProfile() {
        android.util.Log.d("ProfileViewModel", "Forcing user profile refresh")
        userRepository.invalidateCache()
        fetchCurrentUser(true)
    }
    
    fun updateUserProfile(
        username: String,
        displayName: String,
        bio: String?,
        favoriteGenre: String?,
        profileImageUrl: String?
    ) {
        viewModelScope.launch {
            _updateProfileState.value = Resource.Loading()
            try {
                val uid = getAuthStatusUseCase.getCurrentUserId()
                if (uid == null) {
                    _updateProfileState.value = Resource.Error("User ID not available")
                    return@launch
                }
                
                val request = UserProfileRequest(
                    firebaseUid = uid,
                    username = username,
                    displayName = displayName,
                    bio = bio,
                    favoriteGenre = favoriteGenre,
                    profileImageUrl = profileImageUrl
                )
                
                userRepository.updateUserProfile(request).collect { result ->
                    _updateProfileState.value = result
                    if (result is Resource.Success) {
                        // Refresh user data after successful update
                        fetchCurrentUser()
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _updateProfileState.value = Resource.Error(e.message ?: "Failed to update profile")
            }
        }
    }
    
    /**
     * Upload avatar image to cloudinary
     * @param context Application context
     * @param imageUri Uri of the selected image
     */
    fun uploadAvatar(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _avatarUploadState.value = Resource.Loading()
            try {
                val result = cloudinaryService.uploadImage(context, imageUri)
                _avatarUploadState.value = result
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _avatarUploadState.value = Resource.Error(e.message ?: "Failed to upload image")
            }
        }
    }
    
    fun resetUpdateState() {
        _updateProfileState.value = null  // Using null instead of non-existent Idle state
    }
    
    fun resetAvatarUploadState() {
        _avatarUploadState.value = null
    }
    
    // Functions to get user information from current user state
    fun getCurrentUserResponse(): UserResponse? {
        return (userProfileState.value as? Resource.Success)?.data
    }

    fun getUserUsername(): String {
        return getCurrentUserResponse()?.username ?: ""
    }
    
    fun getUserDisplayName(): String {
        return getCurrentUserResponse()?.displayName ?: ""
    }
    
    fun getUserEmail(): String {
        return getCurrentUserResponse()?.email ?: getAuthStatusUseCase.getCurrentUserEmail() ?: ""
    }
    
    fun getUserBio(): String {
        return getCurrentUserResponse()?.bio ?: ""
    }
    
    fun getUserFavoriteGenre(): String {
        return getCurrentUserResponse()?.favoriteGenre ?: ""
    }
    
    fun getUserProfileImageUrl(): String? {
        return getCurrentUserResponse()?.profileImageUrl
    }
    
    fun getUserFullName(): String {
        return getCurrentUserResponse()?.displayName ?: "Loading..."
    }
    
    fun getUserAccountType(): String {
        return "Premium"
    }
    
    fun getUserSubscriptionDate(): String {
        return "January 15, 2025"
    }
    
    fun getUserNextBillingDate(): String {
        return "May 15, 2025"
    }
    
    fun getUserPaymentMethod(): String {
        return "Credit Card (•••• 1234)"
    }
    
    // Toggle settings
    fun toggleNotifications(enabled: Boolean) {
        _notificationEnabled.value = enabled
    }
    
    fun toggleDownloadOnWifiOnly(enabled: Boolean) {
        _downloadOnWifiOnly.value = enabled
    }
    
    fun toggleAutoplay(enabled: Boolean) {
        _enableAutoplay.value = enabled
    }
    
    fun toggleSubtitles(enabled: Boolean) {
        _enableSubtitles.value = enabled
    }
    
    fun setSelectedLanguage(language: String) {
        viewModelScope.launch {
            val languageCode = languageManager.getLanguageCodeFromName(language)
            languageManager.setAppLanguage(languageCode)
        }
    }
    
    // Language options
    fun getAvailableLanguages(): List<String> {
        return languageManager.getAvailableLanguages().keys.toList().sorted()
    }
}