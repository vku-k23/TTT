package com.ttt.cinevibe.presentation.userProfile.connections

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun PendingRequestsScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: UserConnectionViewModel = hiltViewModel()
) {
    val pendingRequestsState by viewModel.pendingRequests.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    
    LaunchedEffect(key1 = userId) {
        viewModel.loadPendingRequests(true)
        
        // Listen for connection events
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
    
    // Load more when reaching the end of list
    LaunchedEffect(listState) {
        if (pendingRequestsState is Resource.Success) {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            
            if (lastVisibleItem >= totalItems - 5 && totalItems > 0) {
                viewModel.loadPendingRequests(false)
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
                    text = "Follow Requests",
                    color = White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            
            // Pending Requests Content
            when (val pendingRequests = pendingRequestsState) {
                is Resource.Loading -> {
                    LoadingIndicator()
                }
                
                is Resource.Success -> {
                    val requestsList = pendingRequests.data?.content ?: emptyList()
                    
                    if (requestsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No pending follow requests",
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
                            items(requestsList.size) { index ->
                                PendingRequestItem(
                                    connection = requestsList[index],
                                    onAccept = { viewModel.acceptFollowRequest(requestsList[index].id) },
                                    onReject = { viewModel.rejectFollowRequest(requestsList[index].id) },
                                    onProfileClick = { onNavigateToProfile(requestsList[index].followerUid) }
                                )
                                
                                if (index < requestsList.size - 1) {
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
                        message = pendingRequests.message ?: "Failed to load follow requests",
                        onRetry = { viewModel.loadPendingRequests(true) }
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
fun PendingRequestItem(
    connection: UserConnectionResponse,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onProfileClick: () -> Unit
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
            if (connection.followerProfileImageUrl != null) {
                AsyncImage(
                    model = connection.followerProfileImageUrl,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = connection.followerName.firstOrNull()?.uppercase() ?: "?",
                    color = White,
                    fontSize = 20.sp
                )
            }
        }
        
        // User info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = connection.followerName,
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Wants to follow you",
                color = LightGray,
                fontSize = 14.sp
            )
        }
        
        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Accept button
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NetflixRed
                ),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(text = "Accept", fontSize = 12.sp)
            }
            
            // Reject button
            OutlinedButton(
                onClick = onReject,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = White
                )
            ) {
                Text(text = "Reject", fontSize = 12.sp)
            }
        }
    }
}