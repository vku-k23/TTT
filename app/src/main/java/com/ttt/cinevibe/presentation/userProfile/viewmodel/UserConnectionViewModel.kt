package com.ttt.cinevibe.presentation.userProfile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.data.remote.models.PageResponse
import com.ttt.cinevibe.data.remote.models.UserConnectionResponse
import com.ttt.cinevibe.data.remote.models.UserResponse
import com.ttt.cinevibe.data.repository.UserConnectionRepository
import com.ttt.cinevibe.data.repository.UserRepository
import com.ttt.cinevibe.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserConnectionViewModel @Inject constructor(
    private val userConnectionRepository: UserConnectionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // State for following
    private val _following = MutableStateFlow<Resource<PageResponse<UserConnectionResponse>>>(Resource.Loading())
    val following: StateFlow<Resource<PageResponse<UserConnectionResponse>>> = _following
    
    // State for user-specific following
    private val _userFollowing = MutableStateFlow<Resource<PageResponse<UserConnectionResponse>>>(Resource.Loading())
    val userFollowing: StateFlow<Resource<PageResponse<UserConnectionResponse>>> = _userFollowing

    // State for followers
    private val _followers = MutableStateFlow<Resource<PageResponse<UserConnectionResponse>>>(Resource.Loading())
    val followers: StateFlow<Resource<PageResponse<UserConnectionResponse>>> = _followers
    
    // State for user-specific followers
    private val _userFollowers = MutableStateFlow<Resource<PageResponse<UserConnectionResponse>>>(Resource.Loading())
    val userFollowers: StateFlow<Resource<PageResponse<UserConnectionResponse>>> = _userFollowers

    // State for pending follow requests
    private val _pendingRequests = MutableStateFlow<Resource<PageResponse<UserConnectionResponse>>>(Resource.Loading())
    val pendingRequests: StateFlow<Resource<PageResponse<UserConnectionResponse>>> = _pendingRequests

    // State for follow/unfollow actions
    private val _followActionResult = MutableStateFlow<Resource<UserConnectionResponse>?>(null)
    val followActionResult: StateFlow<Resource<UserConnectionResponse>?> = _followActionResult

    // Events for one-time consumable actions
    private val _connectionEvents = MutableSharedFlow<ConnectionEvent>()
    val connectionEvents: SharedFlow<ConnectionEvent> = _connectionEvents.asSharedFlow()

    // Connection status
    private val _connectionStatus = MutableStateFlow<Resource<Map<String, Any>>?>(null)
    val connectionStatus: StateFlow<Resource<Map<String, Any>>?> = _connectionStatus

    // Page tracking
    private var followingPage = 0
    private var followersPage = 0
    private var pendingPage = 0
    private var userFollowingPage = 0
    private var userFollowersPage = 0
    private val pageSize = 20
    
    // Store previous results to append
    private val followingResults = mutableListOf<UserConnectionResponse>()
    private val followersResults = mutableListOf<UserConnectionResponse>()
    private val pendingResults = mutableListOf<UserConnectionResponse>()
    private val userFollowingResults = mutableListOf<UserConnectionResponse>()
    private val userFollowersResults = mutableListOf<UserConnectionResponse>()
    
    // Current user ID being viewed
    private var currentViewedUserId: String? = null

    fun loadFollowing(refresh: Boolean = false) {
        if (refresh) {
            followingPage = 0
            followingResults.clear()
        }
        
        viewModelScope.launch {
            userConnectionRepository.getFollowing(followingPage, pageSize)
                .collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            val newItems = result.data?.content ?: emptyList()
                            followingResults.addAll(newItems)
                            
                            val updatedPage = result.data?.copy(content = followingResults)
                            _following.value = Resource.Success(updatedPage ?: result.data) as Resource<PageResponse<UserConnectionResponse>>
                            
                            followingPage++
                        }
                        is Resource.Error -> _following.value = result
                        is Resource.Loading -> {
                            if (followingPage == 0) {
                                _following.value = Resource.Loading()
                            }
                        }
                    }
                }
        }
    }

    fun loadFollowers(refresh: Boolean = false) {
        if (refresh) {
            followersPage = 0
            followersResults.clear()
        }
        
        viewModelScope.launch {
            userConnectionRepository.getFollowers(followersPage, pageSize)
                .collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            val newItems = result.data?.content ?: emptyList()
                            followersResults.addAll(newItems)
                            
                            val updatedPage = result.data?.copy(content = followersResults)
                            _followers.value = Resource.Success(updatedPage ?: result.data) as Resource<PageResponse<UserConnectionResponse>>
                            
                            followersPage++
                        }
                        is Resource.Error -> _followers.value = result
                        is Resource.Loading -> {
                            if (followersPage == 0) {
                                _followers.value = Resource.Loading()
                            }
                        }
                    }
                }
        }
    }

    fun loadPendingRequests(refresh: Boolean = false) {
        if (refresh) {
            pendingPage = 0
            pendingResults.clear()
        }
        
        viewModelScope.launch {
            userConnectionRepository.getPendingRequests(pendingPage, pageSize)
                .collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            val newItems = result.data?.content ?: emptyList()
                            pendingResults.addAll(newItems)
                            
                            val updatedPage = result.data?.copy(content = pendingResults)
                            _pendingRequests.value = Resource.Success(updatedPage ?: result.data) as Resource<PageResponse<UserConnectionResponse>>
                            
                            pendingPage++
                        }
                        is Resource.Error -> _pendingRequests.value = result
                        is Resource.Loading -> {
                            if (pendingPage == 0) {
                                _pendingRequests.value = Resource.Loading()
                            }
                        }
                    }
                }
        }
    }

    fun loadUserFollowing(userId: String, refresh: Boolean = false) {
        if (userId.isEmpty()) {
            Log.e("UserConnectionViewModel", "Attempted to load following with empty user ID")
            _userFollowing.value = Resource.Error("Invalid user ID")
            return
        }
        
        if (refresh) {
            userFollowingPage = 0
            userFollowingResults.clear()
        }
        
        // Store the current user ID being viewed
        currentViewedUserId = userId
        
        viewModelScope.launch {
            Log.d("UserConnectionViewModel", "Loading following for user $userId: page=$userFollowingPage")
            _userFollowing.value = if (userFollowingPage == 0) Resource.Loading() else _userFollowing.value
            
            userConnectionRepository.getUserFollowing(userId, userFollowingPage, pageSize)
                .collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            val newItems = result.data?.content ?: emptyList()
                            userFollowingResults.addAll(newItems)
                            
                            val updatedPage = result.data?.copy(content = userFollowingResults)
                            _userFollowing.value = Resource.Success(updatedPage ?: result.data) as Resource<PageResponse<UserConnectionResponse>>
                            
                            userFollowingPage++
                        }
                        is Resource.Error -> {
                            _userFollowing.value = result
                        }
                        is Resource.Loading -> {
                            if (userFollowingPage == 0) {
                                _userFollowing.value = Resource.Loading()
                            }
                        }
                    }
                }
        }
    }

    fun loadUserFollowers(userId: String, refresh: Boolean = false) {
        if (userId.isEmpty()) {
            Log.e("UserConnectionViewModel", "Attempted to load followers with empty user ID")
            _userFollowers.value = Resource.Error("Invalid user ID")
            return
        }
        
        if (refresh) {
            userFollowersPage = 0
            userFollowersResults.clear()
        }
        
        // Store the current user ID being viewed
        currentViewedUserId = userId
        
        viewModelScope.launch {
            Log.d("UserConnectionViewModel", "Loading followers for user $userId: page=$userFollowersPage")
            _userFollowers.value = if (userFollowersPage == 0) Resource.Loading() else _userFollowers.value
            
            userConnectionRepository.getUserFollowers(userId, userFollowersPage, pageSize)
                .collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            val newItems = result.data?.content ?: emptyList()
                            userFollowersResults.addAll(newItems)
                            
                            val updatedPage = result.data?.copy(content = userFollowersResults)
                            _userFollowers.value = Resource.Success(updatedPage ?: result.data) as Resource<PageResponse<UserConnectionResponse>>
                            
                            userFollowersPage++
                        }
                        is Resource.Error -> {
                            _userFollowers.value = result
                        }
                        is Resource.Loading -> {
                            if (userFollowersPage == 0) {
                                _userFollowers.value = Resource.Loading()
                            }
                        }
                    }
                }
        }
    }

    fun followUser(targetUserUid: String) {
        viewModelScope.launch {
            _followActionResult.value = Resource.Loading()
            userConnectionRepository.followUser(targetUserUid)
                .collectLatest { result ->
                    _followActionResult.value = result
                    if (result is Resource.Success) {
                        // Không cần gọi checkConnectionStatus nữa, sẽ refresh thông tin từ user profile
                        _connectionEvents.emit(ConnectionEvent.FollowSuccess(targetUserUid))
                        
                        // Refresh current user profile to update following count
                        refreshCurrentUserProfile()
                    } else if (result is Resource.Error) {
                        _connectionEvents.emit(ConnectionEvent.Error(result.message ?: "Error following user"))
                    }
                }
        }
    }

    fun unfollowUser(targetUserUid: String) {
        viewModelScope.launch {
            userConnectionRepository.unfollowUser(targetUserUid)
                .collectLatest { result ->
                    if (result is Resource.Success) {
                        // Không cần gọi checkConnectionStatus nữa, sẽ refresh thông tin từ user profile
                        _connectionEvents.emit(ConnectionEvent.UnfollowSuccess(targetUserUid))
                        
                        // Refresh current user profile to update following count
                        refreshCurrentUserProfile()
                    } else if (result is Resource.Error) {
                        _connectionEvents.emit(ConnectionEvent.Error(result.message ?: "Error unfollowing user"))
                    }
                }
        }
    }

    fun acceptFollowRequest(connectionId: Long) {
        viewModelScope.launch {
            userConnectionRepository.acceptFollowRequest(connectionId)
                .collectLatest { result ->
                    if (result is Resource.Success) {
                        // Lấy targetUserUid từ kết quả để gọi lại API cập nhật trạng thái
                        val targetUserUid = result.data?.followerUid
                        if (targetUserUid != null) {
                            checkConnectionStatus(targetUserUid)
                        }
                        
                        _connectionEvents.emit(ConnectionEvent.AcceptFollowSuccess(connectionId))
                        // Refresh pending requests after accepting
                        loadPendingRequests(true)
                        
                        // Refresh current user profile to update followers count
                        refreshCurrentUserProfile()
                    } else if (result is Resource.Error) {
                        _connectionEvents.emit(ConnectionEvent.Error(result.message ?: "Error accepting follow request"))
                    }
                }
        }
    }

    fun rejectFollowRequest(connectionId: Long) {
        viewModelScope.launch {
            userConnectionRepository.rejectFollowRequest(connectionId)
                .collectLatest { result ->
                    if (result is Resource.Success) {
                        _connectionEvents.emit(ConnectionEvent.RejectFollowSuccess(connectionId))
                        // Refresh pending requests after rejecting
                        loadPendingRequests(true)
                    } else if (result is Resource.Error) {
                        _connectionEvents.emit(ConnectionEvent.Error(result.message ?: "Error rejecting follow request"))
                    }
                }
        }

    }

    fun removeFollower(followerUid: String) {
        viewModelScope.launch {
            userConnectionRepository.removeFollower(followerUid)
                .collectLatest { result ->
                    if (result is Resource.Success) {
                        _connectionEvents.emit(ConnectionEvent.RemoveFollowerSuccess(followerUid))
                        // Refresh followers after removing
                        loadFollowers(true)
                    } else if (result is Resource.Error) {
                        _connectionEvents.emit(ConnectionEvent.Error(result.message ?: "Error removing follower"))
                    }
                }
        }
    }

    fun cancelFollowRequest(targetUserUid: String) {
        viewModelScope.launch {
            userConnectionRepository.cancelFollowRequest(targetUserUid)
                .collectLatest { result ->
                    if (result is Resource.Success) {
                        // Không cần gọi checkConnectionStatus nữa, sẽ refresh thông tin từ user profile
                        _connectionEvents.emit(ConnectionEvent.CancelRequestSuccess(targetUserUid))
                        
                        // Refresh current user profile
                        refreshCurrentUserProfile()
                    } else if (result is Resource.Error) {
                        _connectionEvents.emit(ConnectionEvent.Error(result.message ?: "Error cancelling follow request"))
                    }
                }
        }
    }

    fun checkConnectionStatus(targetUserUid: String) {
        viewModelScope.launch {
            _connectionStatus.value = Resource.Loading()
            userConnectionRepository.checkConnectionStatus(targetUserUid)
                .collectLatest { result ->
                    _connectionStatus.value = result
                }
        }
    }
    
    fun clearConnectionStatus() {
        _connectionStatus.value = null
    }

    fun clearFollowActionResult() {
        _followActionResult.value = null
    }

    private fun refreshCurrentUserProfile() {
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { _ ->
                // Dữ liệu người dùng đã được cập nhật trong repository
            }
        }
    }

    fun isCurrentUser(userId: String): Boolean {
        val currentUserState = userRepository.getCurrentUserSync()
        return if (currentUserState is Resource.Success) {
            val currentUser = currentUserState.data
            currentUser?.firebaseUid == userId
        } else {
            false
        }
    }
    
    fun getCurrentUser(): Resource<UserResponse> {
        return userRepository.getCurrentUserSync()
    }

    sealed class ConnectionEvent {
        data class FollowSuccess(val targetUserUid: String) : ConnectionEvent()
        data class UnfollowSuccess(val targetUserUid: String) : ConnectionEvent()
        data class CancelRequestSuccess(val targetUserUid: String) : ConnectionEvent() 
        data class AcceptFollowSuccess(val connectionId: Long) : ConnectionEvent()
        data class RejectFollowSuccess(val connectionId: Long) : ConnectionEvent()
        data class RemoveFollowerSuccess(val followerUid: String) : ConnectionEvent()
        data class Error(val message: String) : ConnectionEvent()
    }
}