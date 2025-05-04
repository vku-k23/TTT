package com.ttt.cinevibe.presentation.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ttt.cinevibe.data.remote.models.UserResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserProfileScreen(
    viewModel: ProfileUserViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    // UI state for the profile form
    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var favoriteGenre by remember { mutableStateOf("") }
    
    // State for handling UI messages
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Collect user state from the ViewModel
    val userState by viewModel.userState.collectAsState()
    
    // Initialize by loading the current user profile
    LaunchedEffect(key1 = Unit) {
        viewModel.getCurrentUser()
    }
    
    // Update local state when user profile loads
    LaunchedEffect(key1 = userState) {
        if (userState is UserState.Success) {
            val userData = (userState as UserState.Success<UserResponse>).data
            displayName = userData.displayName
            bio = userData.bio ?: ""
            favoriteGenre = userData.favoriteGenre ?: ""
        } else if (userState is UserState.Error) {
            snackbarHostState.showSnackbar(
                message = (userState as UserState.Error).message
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Show loading indicator
            if (userState is UserState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Edit Your Profile",
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    // Display name field
                    TextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Bio field
                    TextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Favorite Genre field
                    TextField(
                        value = favoriteGenre,
                        onValueChange = { favoriteGenre = it },
                        label = { Text("Favorite Genre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Update Profile Button
                    Button(
                        onClick = {
                            viewModel.updateUser(
                                displayName = displayName,
                                bio = bio,
                                favoriteGenre = favoriteGenre
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Profile")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Logout Button
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Logout")
                    }
                }
            }
        }
    }
}