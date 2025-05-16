package com.ttt.cinevibe.presentation.profile

import PrivacyTermsScreen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.ttt.cinevibe.data.remote.models.UserResponse
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.presentation.navigation.Screens
import com.ttt.cinevibe.presentation.reviews.UserReviewsScreen
import com.ttt.cinevibe.presentation.userProfile.connections.FollowersScreen
import com.ttt.cinevibe.presentation.userProfile.connections.FollowingScreen
import com.ttt.cinevibe.presentation.userProfile.viewmodel.UserConnectionViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.util.Log
import com.ttt.cinevibe.utils.KnoxCompatUtil

// Profile graph route constant
const val PROFILE_GRAPH_ROUTE = "profile_graph"

// Profile screen routes
private object ProfileScreens {
    const val MAIN = Screens.PROFILE_ROUTE
    const val GENERAL_SETTING = "general_setting"
    const val ACCOUNT_INFO = "account_info"
    const val APP_SETTINGS = "app_settings"
    const val LANGUAGE_SETTINGS = "language_settings"
    const val HELP_SUPPORT = "help_support"
    const val EDIT_PROFILE = "edit_profile"
    const val PRIVACY_TERMS = "privacy_terms"
    const val USER_AGREEMENT = "user_agreement"
    const val FOLLOWERS = "my_followers"  // New route for current user's followers
    const val FOLLOWING = "my_following"  // New route for current user's following
    const val USER_REVIEWS = "my_reviews" // New route for current user's reviews
}

fun NavGraphBuilder.profileNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    // Define the profile navigation graph
    navigation(
        startDestination = ProfileScreens.MAIN,
        route = PROFILE_GRAPH_ROUTE
    ) {
        // Main profile screen
        composable(route = ProfileScreens.MAIN) {
            val viewModel = hiltViewModel<ProfileViewModel>()
            ProfileScreen(
                navController = navController,
                profileViewModel = viewModel,
                onNavigateToGeneralSetting = { navController.navigate(ProfileScreens.GENERAL_SETTING) },
                onNavigateToEditProfile = { navController.navigate(ProfileScreens.EDIT_PROFILE) },
                onNavigateToFollowers = { navController.navigate(ProfileScreens.FOLLOWERS) },
                onNavigateToFollowing = { navController.navigate(ProfileScreens.FOLLOWING) },
                onNavigateToUserReviews = { navController.navigate(ProfileScreens.USER_REVIEWS) },
            )
        }

        // General Settings screen (SettingsScreen)
        composable(route = ProfileScreens.GENERAL_SETTING) {
            SettingsScreen(
                onNavigateToAccountInfo = { navController.navigate(ProfileScreens.ACCOUNT_INFO) },
                onNavigateToAppSettings = { navController.navigate(ProfileScreens.APP_SETTINGS) },
                onNavigateToLanguageSettings = { navController.navigate(ProfileScreens.LANGUAGE_SETTINGS) },
                onNavigateToHelpSupport = { navController.navigate(ProfileScreens.HELP_SUPPORT) },
                onNavigateToPrivacyTerms = { navController.navigate(ProfileScreens.PRIVACY_TERMS) },
                onNavigateToUserAgreement = { navController.navigate(ProfileScreens.USER_AGREEMENT) },
                onBackPressed = { navController.popBackStack() },
                onLogout = onLogout
            )
        }

        // Account information screen
        composable(route = ProfileScreens.ACCOUNT_INFO) {
            AccountInfoScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        
        // App settings screen
        composable(route = ProfileScreens.APP_SETTINGS) {
            AppSettingsScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        
        // Language settings screen
        composable(route = ProfileScreens.LANGUAGE_SETTINGS) {
            LanguageSettingsScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        
        // Help & Support screen
        composable(route = ProfileScreens.HELP_SUPPORT) {
            HelpSupportScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        
        // Edit profile screen
        composable(route = ProfileScreens.EDIT_PROFILE) {
            val viewModel = hiltViewModel<ProfileViewModel>()
            val mainScreenViewModel = hiltViewModel<ProfileViewModel>(
                navController.getBackStackEntry(ProfileScreens.MAIN)
            )
            
            EditProfileScreen(
                onBackPressed = { navController.popBackStack() },
                onSaveComplete = { 
                    // Explicitly refresh the main profile before popping back
                    mainScreenViewModel.fetchCurrentUser()
                    navController.popBackStack() 
                }
            )
        }
        
        // Privacy & Terms screen
        composable(route = ProfileScreens.PRIVACY_TERMS) {
            PrivacyTermsScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        
        // User Agreement screen
        composable(route = ProfileScreens.USER_AGREEMENT) {
            UserAgreementScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        
        // My Followers screen
        composable(route = ProfileScreens.FOLLOWERS) {
            val viewModel = hiltViewModel<UserConnectionViewModel>()
            val currentUserState = viewModel.getCurrentUser()
            
            if (currentUserState is Resource.Success<UserResponse> && currentUserState.data?.firebaseUid != null) {
                // We have the current user ID
                FollowersScreen(
                    userId = currentUserState.data.firebaseUid,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProfile = { userId -> 
                        navController.navigate(Screens.userProfileRoute(userId))
                    },
                    viewModel = viewModel
                )
            } else {
                // Show loading or error state
                CircularProgressIndicator()
            }
        }
        
        // My Following screen
        composable(route = ProfileScreens.FOLLOWING) {
            val viewModel = hiltViewModel<UserConnectionViewModel>()
            val currentUserState = viewModel.getCurrentUser()
            
            if (currentUserState is Resource.Success<UserResponse> && currentUserState.data?.firebaseUid != null) {
                // We have the current user ID
                FollowingScreen(
                    userId = currentUserState.data.firebaseUid,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProfile = { userId -> 
                        navController.navigate(Screens.userProfileRoute(userId))
                    },
                    viewModel = viewModel
                )
            } else {
                // Show loading or error state
                CircularProgressIndicator()
            }
        }

        // My Reviews screen
        composable(route = ProfileScreens.USER_REVIEWS) {
            val viewModel = hiltViewModel<UserConnectionViewModel>()
            val currentUserState = viewModel.getCurrentUser()
            
            // Use a safer approach with state to handle errors
            val errorState = remember { mutableStateOf(false) }
            val errorMessage = remember { mutableStateOf("") }
            val context = LocalContext.current
            
            if (errorState.value) {
                // Show error UI
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Unable to open reviews",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = errorMessage.value,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Go Back")
                    }
                }
            } else {
                if (currentUserState is Resource.Success<UserResponse> && currentUserState.data?.firebaseUid != null) {
                    // We have the current user ID - show reviews screen
                    UserReviewsScreen(
                        userId = currentUserState.data.firebaseUid,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToMovie = { movieId ->
                            try {
                                navController.navigate(Screens.movieDetailRoute(movieId.toString()))
                            } catch (e: Exception) {
                                // Log navigation error
                                Log.e("profileNavGraph", "Navigation error: ${e.message}", e)
                                errorState.value = true
                                errorMessage.value = "Unable to navigate to movie details"
                            }
                        }
                    )
                    
                    // Run Knox compatibility check in the background
                    LaunchedEffect(Unit) {
                        KnoxCompatUtil.safeKnoxOperation(context) {
                            // Only log the operation, don't prevent showing reviews
                            Log.d("profileNavGraph", "Performing review-related operations with Knox compatibility")
                        }
                    }
                } else {
                    // Show loading or error state
                    CircularProgressIndicator()
                }
            }
        }
    }
}