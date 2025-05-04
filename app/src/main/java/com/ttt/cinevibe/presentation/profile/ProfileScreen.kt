package com.ttt.cinevibe.presentation.profile

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ttt.cinevibe.R
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.presentation.auth.AuthState
import com.ttt.cinevibe.presentation.auth.AuthViewModel
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToAccountInfo: () -> Unit = {},
    onNavigateToAppSettings: () -> Unit = {},
    onNavigateToLanguageSettings: () -> Unit = {},
    onNavigateToHelpSupport: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToPrivacyTerms: () -> Unit = {},
    onNavigateToUserAgreement: () -> Unit = {},
    onLogout: () -> Unit
) {
    val logoutState by viewModel.logoutState.collectAsState()
    val userProfileState by profileViewModel.userProfileState.collectAsState()
    val scrollState = rememberScrollState()
    
    // Effect to handle logout state
    LaunchedEffect(key1 = logoutState) {
        when (logoutState) {
            is AuthState.Success -> {
                viewModel.resetAuthStates()
                onLogout()
            }
            is AuthState.Error -> {
                viewModel.resetAuthStates()
            }
            else -> {}
        }
    }
    
    // Refresh user data when screen is shown
    LaunchedEffect(Unit) {
        profileViewModel.fetchCurrentUser()
    }
    
    Scaffold(
        containerColor = Black,
    ) { paddingValues ->
        if (userProfileState is Resource.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = paddingValues.calculateLeftPadding(layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NetflixRed)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Black)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Header Section with user information
                val user = (userProfileState as? Resource.Success)?.data
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user?.profileImageUrl != null) {
                            AsyncImage(
                                model = user.profileImageUrl,
                                contentDescription = "Profile Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = (user?.displayName?.firstOrNull() ?: "U").toString().uppercase(),
                                color = White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = user?.displayName ?: stringResource(R.string.loading),
                        color = White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = user?.email ?: "",
                        color = LightGray,
                        fontSize = 14.sp
                    )
                    
                    // Edit Profile Button
                    Button(
                        onClick = onNavigateToEditProfile,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .width(160.dp)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkGray,
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.edit_profile),
                            fontSize = 14.sp
                        )
                    }
                    
                    // User Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            value = user?.reviewCount?.toString() ?: "0",
                            label = "Reviews"
                        )
                        
                        StatItem(
                            value = user?.followersCount?.toString() ?: "0",
                            label = "Followers"
                        )
                        
                        StatItem(
                            value = user?.followingCount?.toString() ?: "0",
                            label = "Following"
                        )
                    }
                    
                    // Bio and Favorite Genre
                    if (!user?.bio.isNullOrEmpty() || !user?.favoriteGenre.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = DarkGray.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                if (!user?.bio.isNullOrEmpty()) {
                                    Text(
                                        text = "Bio",
                                        color = NetflixRed,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Text(
                                        text = user?.bio ?: "",
                                        color = White,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                    )
                                }
                                
                                if (!user?.favoriteGenre.isNullOrEmpty()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Movie,
                                            contentDescription = null,
                                            tint = NetflixRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Text(
                                            text = "Favorite Genre: ${user?.favoriteGenre}",
                                            color = White,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Divider(
                    color = DarkGray.copy(alpha = 0.5f),
                    thickness = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                // Menu Items
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    ProfileMenuItem(
                        icon = Icons.Filled.Person,
                        title = stringResource(R.string.account_information),
                        onClick = onNavigateToAccountInfo
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Filled.Settings,
                        title = stringResource(R.string.app_settings),
                        onClick = onNavigateToAppSettings
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Filled.Language,
                        title = stringResource(R.string.change_language),
                        onClick = onNavigateToLanguageSettings
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Filled.Info,
                        title = stringResource(R.string.help_support),
                        onClick = onNavigateToHelpSupport
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Logout Button
                Button(
                    onClick = {
                        viewModel.logout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NetflixRed,
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    enabled = logoutState !is AuthState.Loading
                ) {
                    Text(
                        text = stringResource(R.string.logout),
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Footer Links
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = stringResource(R.string.privacy_terms),
                        color = LightGray,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable { onNavigateToPrivacyTerms() }
                    )
                    
                    Text(
                        text = stringResource(R.string.user_agreement),
                        color = LightGray,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable { onNavigateToUserAgreement() }
                    )
                }
                
                // Show loading indicator if logout is in progress
                if (logoutState is AuthState.Loading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = NetflixRed
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = LightGray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = White,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = title,
            color = White,
            fontSize = 16.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(R.string.navigate),
            tint = LightGray,
            modifier = Modifier.size(24.dp)
        )
    }
    
    Divider(
        color = DarkGray,
        thickness = 0.5.dp,
        modifier = Modifier.fillMaxWidth()
    )
}