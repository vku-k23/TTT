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
import com.ttt.cinevibe.presentation.userProfile.connections.FollowersScreen
import com.ttt.cinevibe.presentation.userProfile.connections.FollowingScreen
import com.ttt.cinevibe.presentation.userProfile.viewmodel.UserConnectionViewModel

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
    }
}