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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
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

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToGeneralSetting: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
) {
    val logoutState by viewModel.logoutState.collectAsState()
    val userProfileState by profileViewModel.userProfileState.collectAsState()
    val scrollState = rememberScrollState()
    
    // Quản lý trạng thái hiển thị xem trước avatar
    var showAvatarPreview by remember { mutableStateOf(false) }
    
    // Refresh user data when screen is shown
    LaunchedEffect(Unit) {
        profileViewModel.fetchCurrentUser()
    }
    
    val user = (userProfileState as? Resource.Success)?.data
    
    // Hiển thị dialog xem trước avatar khi showAvatarPreview = true
    if (showAvatarPreview && user?.profileImageUrl != null) {
        AvatarPreviewDialog(
            avatarUrl = user.profileImageUrl,
            onDismiss = { showAvatarPreview = false }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        if (userProfileState is Resource.Loading) {
            CircularProgressIndicator(
                color = NetflixRed,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Row chỉ chứa icon setting ở góc phải phía trên
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.app_settings),
                        tint = White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onNavigateToGeneralSetting() }
                    )
                }
                
                // Profile Header Section với user information
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture ở giữa với khả năng click để xem trước
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(DarkGray)
                            .clickable { 
                                if (user?.profileImageUrl != null) {
                                    showAvatarPreview = true
                                }
                            },
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
                
                // Menu Items are now moved to Settings screen
                
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

@Composable
fun AvatarPreviewDialog(
    avatarUrl: String?,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false, // Không đóng khi nhấn nút back
            dismissOnClickOutside = false // Không đóng khi nhấp vào bên ngoài
        )
    ) {
        // Toàn bộ màn hình với background blur
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            // Background avatar blur hiệu ứng
            if (avatarUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(-1f) // Đảm bảo nằm dưới avatar chính
                ) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(radius = 25.dp)
                            .alpha(0.8f),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            // Avatar chính phóng to ở giữa màn hình
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(280.dp)
                    .clip(CircleShape)
            ) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Image",
                            color = White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                }
            }
            
            // Nút đóng ở góc trên bên phải
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = White
                )
            }
        }
    }
}