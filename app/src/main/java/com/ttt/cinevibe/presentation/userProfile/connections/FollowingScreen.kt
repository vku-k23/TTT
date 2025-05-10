package com.ttt.cinevibe.presentation.userProfile.connections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ttt.cinevibe.data.remote.models.UserConnectionResponse
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.presentation.userProfile.components.ErrorState
import com.ttt.cinevibe.presentation.userProfile.components.LoadingIndicator
import com.ttt.cinevibe.presentation.userProfile.viewmodel.UserConnectionViewModel
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FollowingScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: UserConnectionViewModel = hiltViewModel()
) {
    // Get the current user ID from the ViewModel's currentUserId
    val isCurrentUserProfile = remember { mutableStateOf(false) }
    
    LaunchedEffect(userId) {
        // This will be set in a more reliable way later
        isCurrentUserProfile.value = viewModel.isCurrentUser(userId)
    }
    
    // Use the appropriate state flow based on whether we're viewing our own profile or someone else's
    val followingState by if (isCurrentUserProfile.value) {
        viewModel.following.collectAsState()
    } else {
        viewModel.userFollowing.collectAsState()
    }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    
    LaunchedEffect(key1 = userId) {
        // Load data based on whether we're viewing our own profile or someone else's
        if (isCurrentUserProfile.value) {
            viewModel.loadFollowing(true)
        } else {
            viewModel.loadUserFollowing(userId, true)
        }
        
        // Listen for connection events (like unfollowing)
        viewModel.connectionEvents.collectLatest { event ->
            when (event) {
                is UserConnectionViewModel.ConnectionEvent.UnfollowSuccess -> {
                    snackbarHostState.showSnackbar("Unfollowed user")
                }
                is UserConnectionViewModel.ConnectionEvent.Error -> {
                    snackbarHostState.showSnackbar("Error: ${event.message}")
                }
                else -> {}
            }
        }
    }
    
    // Load more when reaching the end of list
    LaunchedEffect(listState) {
        if (followingState is Resource.Success) {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            
            if (lastVisibleItem >= totalItems - 5 && totalItems > 0) {
                if (isCurrentUserProfile.value) {
                    viewModel.loadFollowing(false)
                } else {
                    viewModel.loadUserFollowing(userId, false)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = White
                    )
                }
                
                Text(
                    text = "Following",
                    color = White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            
            // Following Content
            when (val following = followingState) {
                is Resource.Loading -> {
                    LoadingIndicator()
                }
                
                is Resource.Success -> {
                    val followingList = following.data?.content ?: emptyList()
                    
                    if (followingList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Not following anyone yet",
                                color = LightGray,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState
                        ) {
                            items(followingList.size) { index ->
                                FollowingItem(
                                    connection = followingList[index],
                                    onUnfollow = { viewModel.unfollowUser(followingList[index].followingUid) },
                                    onProfileClick = { onNavigateToProfile(followingList[index].followingUid) },
                                    showUnfollowButton = isCurrentUserProfile.value
                                )
                                
                                if (index < followingList.size - 1) {
                                    Divider(
                                        color = DarkGray.copy(alpha = 0.5f),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
                
                is Resource.Error -> {
                    ErrorState(
                        message = following.message ?: "Failed to load following users",
                        onRetry = { 
                            if (isCurrentUserProfile.value) {
                                viewModel.loadFollowing(true)
                            } else {
                                viewModel.loadUserFollowing(userId, true)
                            }
                        }
                    )
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun FollowingItem(
    connection: UserConnectionResponse,
    onUnfollow: () -> Unit,
    onProfileClick: () -> Unit,
    showUnfollowButton: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(DarkGray)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            if (connection.followingProfileImageUrl != null) {
                AsyncImage(
                    model = connection.followingProfileImageUrl,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = (connection.followingName.firstOrNull() ?: "U").toString().uppercase(),
                    color = White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // User info
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onProfileClick() }
        ) {
            Text(
                text = connection.followingName,
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Unfollow button - only show for current user's following list
        if (showUnfollowButton) {
            Button(
                onClick = onUnfollow,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NetflixRed,
                    contentColor = White
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = "Unfollow", fontSize = 12.sp)
            }
        }
    }
}