package com.ttt.cinevibe.presentation.profile

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ttt.cinevibe.R
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.MediumGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditProfileScreen(
    onBackPressed: () -> Unit,
    onSaveComplete: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // User profile states
    val userProfileState by profileViewModel.userProfileState.collectAsState()
    val updateProfileState by profileViewModel.updateProfileState.collectAsState()
    val avatarUploadState by profileViewModel.avatarUploadState.collectAsState()

    // Form fields
    var displayName by remember { mutableStateOf(profileViewModel.getUserDisplayName()) }
    var bio by remember { mutableStateOf(profileViewModel.getUserBio()) }
    var favoriteGenre by remember { mutableStateOf(profileViewModel.getUserFavoriteGenre()) }
    var profileImageUrl by remember {
        mutableStateOf(
            profileViewModel.getUserProfileImageUrl() ?: ""
        )
    }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Permission state for accessing images
    val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    // Image picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            profileViewModel.uploadAvatar(context, it)
        }
    }

    // Effect to update form fields when profile data is loaded
    LaunchedEffect(userProfileState) {
        if (userProfileState is Resource.Success) {
            val user = (userProfileState as Resource.Success).data
            if (user != null) {
                displayName = user.displayName
                bio = user.bio ?: ""
                favoriteGenre = user.favoriteGenre ?: ""
                profileImageUrl = user.profileImageUrl ?: ""
            }
        }
    }

    // Effect to handle update state changes
    LaunchedEffect(updateProfileState) {
        when (updateProfileState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Profile updated successfully")
                profileViewModel.resetUpdateState()
                onSaveComplete()
            }

            is Resource.Error -> {
                val message =
                    (updateProfileState as Resource.Error).message ?: "Failed to update profile"
                snackbarHostState.showSnackbar(message)
            }

            else -> {}
        }
    }

    // Effect to handle avatar upload state changes
    LaunchedEffect(avatarUploadState) {
        when (avatarUploadState) {
            is Resource.Success -> {
                val imageUrl = (avatarUploadState as Resource.Success).data
                if (imageUrl != null) {
                    profileImageUrl = imageUrl
                }
                snackbarHostState.showSnackbar("Image uploaded successfully")
                profileViewModel.resetAvatarUploadState()
            }

            is Resource.Error -> {
                val message =
                    (avatarUploadState as Resource.Error).message ?: "Failed to upload image"
                snackbarHostState.showSnackbar(message)
                profileViewModel.resetAvatarUploadState()
            }

            else -> {}
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
        ) {
            ProfileTopBar(
                title = stringResource(R.string.edit_profile),
                onBackPressed = onBackPressed
            )
            
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture Area
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(DarkGray)
                        .clickable {
                            // Check permission before launching gallery
                            if (permissionState.status.isGranted) {
                                galleryLauncher.launch("image/*")
                            } else {
                                permissionState.launchPermissionRequest()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Display selected image or current profile image
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = displayName.firstOrNull()?.uppercase() ?: "U",
                            color = White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Overlay with edit icon
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUploadState is Resource.Loading) {
                            CircularProgressIndicator(
                                color = White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Change profile picture",
                                tint = White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Tap to change profile picture",
                    color = LightGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Form Fields
                ProfileTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = stringResource(R.string.full_name),
                    keyboardType = KeyboardType.Text
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "Bio",
                    keyboardType = KeyboardType.Text,
                    singleLine = false,
                    modifier = Modifier.height(120.dp).fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = favoriteGenre,
                    onValueChange = { favoriteGenre = it },
                    label = "Favorite Genre",
                    keyboardType = KeyboardType.Text
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Save Button
                Button(
                    onClick = {
                        profileViewModel.updateUserProfile(
                            displayName = displayName,
                            bio = bio.ifEmpty { null },
                            favoriteGenre = favoriteGenre.ifEmpty { null },
                            profileImageUrl = profileImageUrl.ifEmpty { null }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NetflixRed,
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    enabled = displayName.isNotEmpty() && updateProfileState !is Resource.Loading
                ) {
                    if (updateProfileState is Resource.Loading) {
                        CircularProgressIndicator(
                            color = White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.save_changes),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = LightGray) },
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = DarkGray.copy(alpha = 0.7f),
            focusedContainerColor = MediumGray.copy(alpha = 0.7f),
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = NetflixRed,
            unfocusedTextColor = White,
            focusedTextColor = White,
            unfocusedLabelColor = LightGray,
            focusedLabelColor = White
        ),
        shape = RoundedCornerShape(4.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        singleLine = singleLine
    )
}