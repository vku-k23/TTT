package com.ttt.cinevibe.presentation.profile

import PrivacyTermsScreen
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.ttt.cinevibe.presentation.navigation.Screens

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
            ProfileScreen(
                navController = navController,
                onNavigateToGeneralSetting = { navController.navigate(ProfileScreens.GENERAL_SETTING) },
                onNavigateToEditProfile = { navController.navigate(ProfileScreens.EDIT_PROFILE) },
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
            EditProfileScreen(
                onBackPressed = { navController.popBackStack() },
                onSaveComplete = { navController.popBackStack() }
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
    }
}