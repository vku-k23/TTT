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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
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
import androidx.compose.runtime.derivedStateOf
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
import com.ttt.cinevibe.ui.theme.ErrorRed
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
    var username by remember { mutableStateOf(profileViewModel.getUserUsername()) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    
    var displayName by remember { mutableStateOf(profileViewModel.getUserDisplayName()) }
    var displayNameError by remember { mutableStateOf<String?>(null) }
    
    var bio by remember { mutableStateOf(profileViewModel.getUserBio()) }
    var bioError by remember { mutableStateOf<String?>(null) }
    
    var favoriteGenre by remember { mutableStateOf(profileViewModel.getUserFavoriteGenre()) }
    var profileImageUrl by remember {
        mutableStateOf(profileViewModel.getUserProfileImageUrl() ?: "")
    }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Form validation state
    val isFormValid by remember {
        derivedStateOf {
            displayName.isNotBlank() && 
            displayNameError == null && 
            username.isNotBlank() && 
            usernameError == null && 
            bioError == null
        }
    }

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
                username = user.username ?: ""
                displayName = user.displayName
                bio = user.bio ?: ""
                favoriteGenre = user.favoriteGenre ?: ""
                profileImageUrl = user.profileImageUrl ?: ""
            }
        }
    }

    // Function to validate form fields
    fun validateFields(): Boolean {
        // Reset errors
        displayNameError = null
        usernameError = null
        bioError = null
        
        // Validate display name
        if (displayName.isBlank()) {
            displayNameError = "Display name cannot be empty"
            return false
        } else if (displayName.length < 3) {
            displayNameError = "Display name must be at least 3 characters"
            return false
        }
        
        // Validate username
        if (username.isBlank()) {
            usernameError = "Username cannot be empty"
            return false
        } else if (username.length < 3) {
            usernameError = "Username must be at least 3 characters"
            return false
        } else if (!username.matches("^[a-zA-Z0-9_]+$".toRegex())) {
            usernameError = "Username can only contain letters, numbers and underscore"
            return false
        }
        
        // Validate bio (optional field)
        if (bio.isNotBlank() && bio.length > 200) {
            bioError = "Bio cannot exceed 200 characters"
            return false
        }
        
        return true
    }

    // Effect to handle update state changes
    LaunchedEffect(updateProfileState) {
        when (updateProfileState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Profile updated successfully")
                // Force a complete refresh of profile data from server
                profileViewModel.refreshUserProfile()
                profileViewModel.resetUpdateState()
                onSaveComplete()
            }

            is Resource.Error -> {
                val message =
                    (updateProfileState as Resource.Error).message ?: "Failed to update profile"
                snackbarHostState.showSnackbar(message)
                profileViewModel.resetUpdateState()
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
            TopAppBar(
                title = { Text(text = stringResource(R.string.edit_profile), color = White) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Black.copy(alpha = 0.8f)
                ),
                modifier = Modifier.systemBarsPadding() // Using systemBarsPadding instead of statusBarsPadding
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
                    value = username,
                    onValueChange = { 
                        username = it
                        if (usernameError != null && it.isNotBlank()) {
                            if (it.length >= 3 && it.matches("^[a-zA-Z0-9_]+$".toRegex())) {
                                usernameError = null
                            }
                        }
                    },
                    label = stringResource(R.string.username),
                    keyboardType = KeyboardType.Text,
                    isError = usernameError != null,
                    errorMessage = usernameError
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = displayName,
                    onValueChange = { 
                        displayName = it 
                        if (displayNameError != null && it.isNotBlank() && it.length >= 3) {
                            displayNameError = null
                        }
                    },
                    label = stringResource(R.string.full_name),
                    keyboardType = KeyboardType.Text,
                    isError = displayNameError != null,
                    errorMessage = displayNameError
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = bio,
                    onValueChange = { 
                        bio = it
                        if (bioError != null && it.length <= 200) {
                            bioError = null
                        }
                    },
                    label = stringResource(R.string.bio),
                    keyboardType = KeyboardType.Text,
                    singleLine = false,
                    modifier = Modifier.height(120.dp).fillMaxWidth(),
                    isError = bioError != null,
                    errorMessage = bioError,
                    maxChar = 200
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = favoriteGenre,
                    onValueChange = { favoriteGenre = it },
                    label = stringResource(R.string.favorite_genre),
                    keyboardType = KeyboardType.Text
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Save Button
                Button(
                    onClick = {
                        if (validateFields()) {
                            profileViewModel.updateUserProfile(
                                username = username.trim(),
                                displayName = displayName.trim(),
                                bio = bio.trim().ifEmpty { null },
                                favoriteGenre = favoriteGenre.trim().ifEmpty { null },
                                profileImageUrl = profileImageUrl.trim().ifEmpty { null }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NetflixRed,
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    enabled = isFormValid && updateProfileState !is Resource.Loading
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
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
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
        .height(60.dp),
    isError: Boolean = false,
    errorMessage: String? = null,
    maxChar: Int? = null
) {
    Column {
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
                focusedLabelColor = White,
                errorContainerColor = DarkGray.copy(alpha = 0.7f),
                errorIndicatorColor = ErrorRed,
                errorTextColor = White,
                errorLabelColor = ErrorRed
            ),
            shape = RoundedCornerShape(4.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            singleLine = singleLine,
            isError = isError,
            trailingIcon = {
                if (isError) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = ErrorRed
                    )
                } else if (maxChar != null) {
                    Text(
                        text = "${value.length}/$maxChar",
                        color = if (value.length > maxChar) ErrorRed else LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        )
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = ErrorRed,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}