package com.ttt.cinevibe.presentation.userProfile.discover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ttt.cinevibe.data.remote.models.PageResponse
import com.ttt.cinevibe.data.remote.models.UserProfileResponse
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.presentation.userProfile.components.EmptyState
import com.ttt.cinevibe.presentation.userProfile.components.ErrorState
import com.ttt.cinevibe.presentation.userProfile.components.LoadingIndicator
import com.ttt.cinevibe.presentation.userProfile.components.UserProfileCard
import com.ttt.cinevibe.presentation.userProfile.viewmodel.UserRecommendationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRecommendationScreen(
    onUserClick: (userId: String) -> Unit,
    viewModel: UserRecommendationViewModel = hiltViewModel()
) {
    val recommendedUsersState by viewModel.recommendedUsers.collectAsState()
    val searchResultsState by viewModel.searchResults.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // Search bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    if (it.isBlank()) {
                        isSearchActive = false
                        viewModel.clearSearch()
                    } else {
                        isSearchActive = true
                        viewModel.searchUsers(it)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = { 
                    Text("Search users...", style = MaterialTheme.typography.bodyMedium) 
                },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        contentDescription = "Search",
                        modifier = Modifier.padding(start = 8.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            isSearchActive = false
                            viewModel.clearSearch()
                        }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    containerColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
        
        // Content based on state
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isSearchActive -> {
                    // Search results
                    SearchResults(
                        searchResultsState = searchResultsState,
                        searchQuery = searchQuery,
                        onUserClick = onUserClick,
                        onRetry = { viewModel.searchUsers(searchQuery) }
                    )
                }
                else -> {
                    // Recommended users
                    RecommendedUsers(
                        recommendedUsersState = recommendedUsersState,
                        onUserClick = onUserClick,
                        onRetry = { viewModel.loadRecommendedUsers(true) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResults(
    searchResultsState: Resource<PageResponse<UserProfileResponse>>,
    searchQuery: String,
    onUserClick: (userId: String) -> Unit,
    onRetry: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        when (searchResultsState) {
            is Resource.Loading -> {
                LoadingIndicator()
            }
            is Resource.Success -> {
                val users = searchResultsState.data?.content
                if (users.isNullOrEmpty()) {
                    EmptyState(message = "No users found for \"$searchQuery\"")
                } else {
                    Column {
                        Text(
                            text = "Search Results",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                        )
                        UsersList(
                            users = users,
                            onUserClick = onUserClick
                        )
                    }
                }
            }
            is Resource.Error -> {
                ErrorState(
                    message = searchResultsState.message ?: "Error loading search results",
                    onRetry = onRetry
                )
            }
        }
    }
}

@Composable
fun RecommendedUsers(
    recommendedUsersState: Resource<PageResponse<UserProfileResponse>>,
    onUserClick: (userId: String) -> Unit,
    onRetry: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        when (recommendedUsersState) {
            is Resource.Loading -> {
                LoadingIndicator()
            }
            is Resource.Success -> {
                val users = recommendedUsersState.data?.content
                if (users.isNullOrEmpty()) {
                    EmptyState(message = "No recommendations available")
                } else {
                    Column {
                        Text(
                            text = "Suggested for you",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                        )
                        UsersList(
                            users = users,
                            onUserClick = onUserClick
                        )
                    }
                }
            }
            is Resource.Error -> {
                ErrorState(
                    message = recommendedUsersState.message ?: "Error loading recommendations",
                    onRetry = onRetry
                )
            }
        }
    }
}

@Composable
fun UsersList(
    users: List<UserProfileResponse>,
    onUserClick: (userId: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users) { user ->
            UserProfileCard(
                userId = user.firebaseUid,
                displayName = user.displayName,
                username = user.username,
                profileImageUrl = user.profileImageUrl,
                bio = user.bio,
                connectionStatus = user.connectionStatus,
                onClick = { onUserClick(user.firebaseUid) }
            )
        }
    }
} 