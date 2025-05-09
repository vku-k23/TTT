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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onFollowUser: (userId: String) -> Unit,
    onShareProfile: (userId: String) -> Unit = {},
    onMessageUser: (userId: String) -> Unit = {},
    viewModel: UserRecommendationViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) {
        viewModel.getUserProfile(userId)
    }
    var showAvatarPreview by remember { mutableStateOf(false) }
    val userProfileState by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()


    if (showAvatarPreview && userProfileState.data!!.profileImageUrl != null) {
        AvatarPreviewDialog(
            avatarUrl = userProfileState.data!!.profileImageUrl,
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
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Username
                        Text(
                            text = "@${userProfile.username}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bio
                        if (!userProfile.bio.isNullOrEmpty()) {
                            Text(
                                text = userProfile.bio,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats
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
                                    label = "Followers"
                                )

                                StatItem(
                                    value = userProfile.followingCount?.toString() ?: "0",
                                    label = "Following"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Follow and Message buttons
                        if (!userProfile.isCurrentUser) {
                            val buttonText = when (userProfile.connectionStatus) {
                                "ACCEPTED" -> "Following"
                                "PENDING" -> "Requested"
                                else -> "Follow"
                            }

                            val isFollowing = userProfile.connectionStatus == "ACCEPTED"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Follow button
                                Button(
                                    onClick = { onFollowUser(userId) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = if (isFollowing) {
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

                        // Favorite genre section if available
                        userProfile.favoriteGenre?.let { genre ->
                            if (genre.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                            alpha = 0.5f
                                        )
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Favorite Genre",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = genre,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
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
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = count,
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