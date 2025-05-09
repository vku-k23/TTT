package com.ttt.cinevibe.presentation.userProfile.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.presentation.userProfile.components.ErrorState
import com.ttt.cinevibe.presentation.userProfile.components.LoadingIndicator
import com.ttt.cinevibe.presentation.userProfile.viewmodel.UserRecommendationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onFollowUser: (userId: String) -> Unit,
    viewModel: UserRecommendationViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) {
        viewModel.getUserProfile(userId)
    }
    
    val userProfileState by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val profileState = userProfileState) {
                is Resource.Loading -> {
                    LoadingIndicator()
                }
                
                is Resource.Success -> {
                    val userProfile = profileState.data!!
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Profile image
                        val painter = if (userProfile.profileImageUrl.isNullOrEmpty()) {
                            rememberVectorPainter(Icons.Default.Person)
                        } else {
                            rememberAsyncImagePainter(userProfile.profileImageUrl)
                        }
                        
                        Surface(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            shadowElevation = 4.dp,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Image(
                                painter = painter,
                                contentDescription = "Profile image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Display name
                        Text(
                            text = userProfile.displayName,
                            style = MaterialTheme.typography.headlineSmall,
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
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Stats
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Reviews
                                StatItem(
                                    count = userProfile.reviewCount?.toString() ?: "0",
                                    label = "Reviews"
                                )
                                
                                Divider(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .width(1.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                )
                                
                                // Followers
                                StatItem(
                                    count = userProfile.followersCount?.toString() ?: "0",
                                    label = "Followers"
                                )
                                
                                Divider(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .width(1.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                )
                                
                                // Following
                                StatItem(
                                    count = userProfile.followingCount?.toString() ?: "0",
                                    label = "Following"
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Follow button
                        if (!userProfile.isCurrentUser) {
                            val buttonText = when (userProfile.connectionStatus) {
                                "ACCEPTED" -> "Following"
                                "PENDING" -> "Requested"
                                else -> "Follow"
                            }
                            
                            val isFollowing = userProfile.connectionStatus == "ACCEPTED"
                            
                            Button(
                                onClick = { onFollowUser(userId) },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = if (isFollowing) {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.primary
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
                        }
                        
                        // Favorite genre section if available
                        userProfile.favoriteGenre?.let { genre ->
                            if (genre.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
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