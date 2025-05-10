package com.ttt.cinevibe.presentation.notifications

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    onNavigateToProfile: (String) -> Unit,
    viewModel: UserConnectionViewModel = hiltViewModel()
) {
    val pendingRequestsState by viewModel.pendingRequests.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(key1 = Unit) {
        viewModel.loadPendingRequests(true)
        
        // Collect events
        viewModel.connectionEvents.collectLatest { event ->
            when (event) {
                is UserConnectionViewModel.ConnectionEvent.AcceptFollowSuccess -> {
                    snackbarHostState.showSnackbar("Follow request accepted")
                }
                is UserConnectionViewModel.ConnectionEvent.RejectFollowSuccess -> {
                    snackbarHostState.showSnackbar("Follow request rejected")
                }
                is UserConnectionViewModel.ConnectionEvent.Error -> {
                    snackbarHostState.showSnackbar("Error: ${event.message}")
                }
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        // Notifications Header
        Text(
            text = "Notifications",
            color = White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        
        // Notifications Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (val pendingRequests = pendingRequestsState) {
                is Resource.Loading -> {
                    LoadingIndicator()
                }
                
                is Resource.Success -> {
                    val requests = pendingRequests.data?.content ?: emptyList()
                    if (requests.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No notifications yet",
                                color = LightGray,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = "Follow Requests",
                                    color = White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            items(requests.size) { index ->
                                FollowRequestItem(
                                    connection = requests[index],
                                    onAccept = { viewModel.acceptFollowRequest(requests[index].id) },
                                    onReject = { viewModel.rejectFollowRequest(requests[index].id) },
                                    onProfileClick = { onNavigateToProfile(requests[index].followerUid) }
                                )
                                
                                if (index < requests.size - 1) {
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
                        message = pendingRequests.message ?: "Failed to load notifications",
                        onRetry = { viewModel.loadPendingRequests(true) }
                    )
                }
            }
            
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun FollowRequestItem(
    connection: UserConnectionResponse,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onProfileClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    
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
            if (connection.followerProfileImageUrl != null) {
                AsyncImage(
                    model = connection.followerProfileImageUrl,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = (connection.followerName.firstOrNull() ?: "U").toString().uppercase(),
                    color = White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Notification content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = connection.followerName,
                    color = White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onProfileClick() }
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "wants to follow you",
                    color = LightGray,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = dateFormat.format(connection.createdAt),
                color = LightGray,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onAccept,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NetflixRed,
                        contentColor = White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Text(text = "Accept", fontSize = 12.sp)
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                OutlinedButton(
                    onClick = onReject,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Text(text = "Decline", fontSize = 12.sp)
                }
            }
        }
    }
}

// Keep existing NotificationItem and NotificationType classes for other types of notifications
data class NotificationItem(
    val id: Int,
    val type: NotificationType,
    val senderName: String,
    val message: String,
    val timestamp: Date,
    val isRead: Boolean = false
)

enum class NotificationType {
    FOLLOW_REQUEST,
    FOLLOW_ACCEPTED,
    LIKE,
    COMMENT,
    RECOMMEND
}