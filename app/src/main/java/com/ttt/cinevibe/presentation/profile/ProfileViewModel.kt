package com.ttt.cinevibe.presentation.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    
    // Settings state flows
    private val _notificationEnabled = MutableStateFlow(true)
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled
    
    private val _downloadOnWifiOnly = MutableStateFlow(true)
    val downloadOnWifiOnly: StateFlow<Boolean> = _downloadOnWifiOnly
    
    private val _enableAutoplay = MutableStateFlow(true)
    val enableAutoplay: StateFlow<Boolean> = _enableAutoplay
    
    private val _enableSubtitles = MutableStateFlow(false)
    val enableSubtitles: StateFlow<Boolean> = _enableSubtitles
    
    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage
    
    // Functions to get user information
    // In a real app, these would fetch from a repository or API
    
    fun getUserFullName(): String {
        return "John Doe"
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
        _selectedLanguage.value = language
    }
    
    // Language options
    fun getAvailableLanguages(): List<String> {
        return listOf(
            "English",
            "Spanish",
            "French",
            "German",
            "Italian",
            "Japanese",
            "Korean",
            "Mandarin",
            "Hindi",
            "Arabic",
            "Portuguese",
            "Russian"
        )
    }
}