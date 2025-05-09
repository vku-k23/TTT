package com.ttt.cinevibe.presentation.userProfile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.data.remote.models.PageResponse
import com.ttt.cinevibe.data.remote.models.UserProfileResponse
import com.ttt.cinevibe.data.repository.UserRecommendationRepository
import com.ttt.cinevibe.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserRecommendationViewModel @Inject constructor(
    private val userRecommendationRepository: UserRecommendationRepository
) : ViewModel() {

    // State for recommended users
    private val _recommendedUsers = MutableStateFlow<Resource<PageResponse<UserProfileResponse>>>(Resource.Loading())
    val recommendedUsers: StateFlow<Resource<PageResponse<UserProfileResponse>>> = _recommendedUsers
    
    // State for user profile
    private val _userProfile = MutableStateFlow<Resource<UserProfileResponse>>(Resource.Loading())
    val userProfile: StateFlow<Resource<UserProfileResponse>> = _userProfile
    
    // State for search results
    private val _searchResults = MutableStateFlow<Resource<PageResponse<UserProfileResponse>>>(Resource.Loading())
    val searchResults: StateFlow<Resource<PageResponse<UserProfileResponse>>> = _searchResults
    
    // Current page for pagination
    private var currentPage = 0
    private val pageSize = 10
    
    init {
        loadRecommendedUsers()
    }
    
    fun loadRecommendedUsers(refresh: Boolean = false) {
        if (refresh) {
            currentPage = 0
        }
        
        viewModelScope.launch {
            userRecommendationRepository.getRecommendedUsers(currentPage, pageSize)
                .collectLatest { result ->
                    _recommendedUsers.value = result
                }
        }
    }
    
    fun loadNextPage() {
        currentPage++
        loadRecommendedUsers()
    }
    
    fun getUserProfile(firebaseUid: String) {
        viewModelScope.launch {
            userRecommendationRepository.getUserProfile(firebaseUid)
                .collectLatest { result ->
                    _userProfile.value = result
                }
        }
    }
    
    fun searchUsers(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = Resource.Error("Search query cannot be empty")
                return@launch
            }
            
            userRecommendationRepository.searchUsers(query, 0, pageSize)
                .collectLatest { result ->
                    _searchResults.value = result
                }
        }
    }
    
    fun clearSearch() {
        _searchResults.value = Resource.Loading()
    }
} 