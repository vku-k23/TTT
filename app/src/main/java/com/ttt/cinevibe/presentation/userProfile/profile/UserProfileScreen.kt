package com.ttt.cinevibe.presentation.userProfile.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.ttt.cinevibe.R
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.presentation.profile.AvatarPreviewDialog
import com.ttt.cinevibe.presentation.profile.ProfileTopBar
import com.ttt.cinevibe.presentation.profile.StatItem
import com.ttt.cinevibe.presentation.userProfile.components.ErrorState
import com.ttt.cinevibe.presentation.userProfile.components.LoadingIndicator
import com.ttt.cinevibe.presentation.userProfile.viewmodel.UserRecommendationViewModel
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White
import com.ttt.cinevibe.presentation.userProfile.viewmodel.UserConnectionViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToFollowers: (String) -> Unit = {},
    onNavigateToFollowing: (String) -> Unit = {},
    onNavigateToPendingRequests: (String) -> Unit = {}, // Thêm tham số mới cho điều hướng đến yêu cầu theo dõi
    onShareProfile: (userId: String) -> Unit = {},
    onMessageUser: (userId: String) -> Unit = {},
    viewModel: UserRecommendationViewModel = hiltViewModel(),
    connectionViewModel: UserConnectionViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) {
        viewModel.getUserProfile(userId)
        // Không cần gọi checkConnectionStatus ở đây nữa, vì chúng ta sẽ sử dụng connectionStatus từ profile
    }
    var showAvatarPreview by remember { mutableStateOf(false) }
    val userProfileState by viewModel.userProfile.collectAsState()
    val connectionStatusState by connectionViewModel.connectionStatus.collectAsState()
    val followActionResultState by connectionViewModel.followActionResult.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Track connection events
    LaunchedEffect(Unit) {
        connectionViewModel.connectionEvents.collectLatest { event ->
            when (event) {
                is UserConnectionViewModel.ConnectionEvent.FollowSuccess -> {
                    snackbarHostState.showSnackbar("Follow request sent")
                    // Refresh the profile to update UI
                    viewModel.getUserProfile(userId)
                }
                is UserConnectionViewModel.ConnectionEvent.UnfollowSuccess -> {
                    snackbarHostState.showSnackbar("Unfollowed user")
                    // Refresh the profile to update UI
                    viewModel.getUserProfile(userId)
                }
                is UserConnectionViewModel.ConnectionEvent.CancelRequestSuccess -> {
                    snackbarHostState.showSnackbar("Follow request cancelled")
                    // Refresh the profile to update UI
                    viewModel.getUserProfile(userId)
                }
                is UserConnectionViewModel.ConnectionEvent.Error -> {
                    snackbarHostState.showSnackbar("Error: ${event.message}")
                }
                else -> {}
            }
        }
    }

    if (showAvatarPreview && userProfileState is Resource.Success && 
        (userProfileState as Resource.Success).data?.profileImageUrl != null) {
        AvatarPreviewDialog(
            avatarUrl = (userProfileState as Resource.Success).data?.profileImageUrl,
            onDismiss = { showAvatarPreview = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Custom top bar with share button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = White
                    )
                }
                
                IconButton(onClick = { onShareProfile(userId) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share profile",
                        tint = White
                    )
                }
            }
            
            when (val profileState = userProfileState) {
                is Resource.Loading -> {
                    LoadingIndicator()
                }

                is Resource.Success -> {
                    val userProfile = profileState.data!!
                    // Sử dụng connectionStatus từ userProfile thay vì từ connectionStatusState
                    val connectionStatus = userProfile.connectionStatus ?: "NONE"
                    // Xác định isFollowing dựa trên connectionStatus
                    val isFollowing = connectionStatus == "ACCEPTED"

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(DarkGray)
                                .clickable {
                                    if (userProfile.profileImageUrl != null) {
                                        showAvatarPreview = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (userProfile.profileImageUrl != null) {
                                AsyncImage(
                                    model = userProfile.profileImageUrl,
                                    contentDescription = "Profile Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = (userProfile.displayName.firstOrNull() ?: "U").toString()
                                        .uppercase(),
                                    color = White,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = userProfile.displayName,
                            color = White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Username
                        Text(
                            text = "@${userProfile.username}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Stats with clickable followers/following
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(
                                    value = userProfile.reviewCount?.toString() ?: "0",
                                    label = "Reviews"
                                )

                                StatItem(
                                    value = userProfile.followersCount?.toString() ?: "0",
                                    label = "Followers",
                                    onClick = { onNavigateToFollowers(userId) }
                                )

                                StatItem(
                                    value = userProfile.followingCount?.toString() ?: "0",
                                    label = "Following",
                                    onClick = { onNavigateToFollowing(userId) }
                                )
                            }
                        }

                        // Nút cho yêu cầu theo dõi đang chờ nếu là trang cá nhân của người dùng hiện tại
                        if (userProfile.isCurrentUser) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Thêm biến theo dõi số lượng yêu cầu đang chờ
                            val pendingRequestsViewModel: UserConnectionViewModel = hiltViewModel()
                            val pendingRequestsState by pendingRequestsViewModel.pendingRequests.collectAsState()
                            
                            // Tải yêu cầu theo dõi đang chờ khi hồ sơ được hiển thị
                            LaunchedEffect(userId) {
                                pendingRequestsViewModel.loadPendingRequests(true)
                            }
                            
                            // Tính toán số lượng yêu cầu đang chờ
                            val pendingCount = when (pendingRequestsState) {
                                is Resource.Success -> {
                                    (pendingRequestsState as Resource.Success).data?.content?.size ?: 0
                                }
                                else -> 0
                            }
                            
                            OutlinedButton(
                                onClick = { onNavigateToPendingRequests(userId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp)
                                    .height(42.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Black,
                                    contentColor = White
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Pending Follow Requests",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    
                                    if (pendingCount > 0) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(
                                                    color = NetflixRed,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (pendingCount > 99) "99+" else pendingCount.toString(),
                                                color = White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Follow and Message buttons
                        if (!userProfile.isCurrentUser) {
                            val buttonText = when (connectionStatus) {
                                "ACCEPTED" -> "Following"
                                "PENDING" -> "Cancel Request"
                                else -> "Follow"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Follow button
                                Button(
                                    onClick = { 
                                        when (connectionStatus) {
                                            "ACCEPTED" -> connectionViewModel.unfollowUser(userId)
                                            "PENDING" -> connectionViewModel.cancelFollowRequest(userId)
                                            else -> connectionViewModel.followUser(userId)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = if (connectionStatus == "ACCEPTED" || connectionStatus == "PENDING") {
                                        ButtonDefaults.buttonColors(
                                            containerColor = NetflixRed,
                                            contentColor = White
                                        )
                                    } else {
                                        ButtonDefaults.buttonColors()
                                    }
                                ) {
                                    Text(
                                        text = buttonText,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                                
                                // Message button
                                OutlinedButton(
                                    onClick = { onMessageUser(userId) },
                                    modifier = Modifier
                                        .height(42.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Black,
                                        contentColor = White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Send,
                                        contentDescription = "Message",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Message",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bio
                        if (!userProfile.bio.isNullOrEmpty()) {
                            Text(
                                text = userProfile.bio,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        // Favorite genre section if available
                        userProfile.favoriteGenre?.let { genre ->
                            if (genre.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Movie,
                                                contentDescription = null,
                                                tint = NetflixRed,
                                                modifier = Modifier.size(26.dp)
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = userProfile.favoriteGenre,
                                                color = White,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                is Resource.Error -> {
                    ErrorState(
                        message = profileState.message ?: "Failed to load user profile",
                        onRetry = { viewModel.getUserProfile(userId) }
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
fun StatItem(
    value: String, 
    label: String,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}