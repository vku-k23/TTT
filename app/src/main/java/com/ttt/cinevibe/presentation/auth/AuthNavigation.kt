package com.ttt.cinevibe.presentation.auth

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ttt.cinevibe.presentation.auth.screens.AddCardDetailsScreen
import com.ttt.cinevibe.presentation.auth.screens.GettingStartedScreen
import com.ttt.cinevibe.presentation.auth.screens.LoginScreen
import com.ttt.cinevibe.presentation.auth.screens.RegisterScreen
import com.ttt.cinevibe.presentation.auth.screens.SplashScreen

object AuthDestinations {
    const val SPLASH_ROUTE = "splash"
    const val GETTING_STARTED_ROUTE = "getting_started"
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val ADD_CARD_DETAILS_ROUTE = "add_card_details"
    const val MAIN_CONTENT_ROUTE = "main_content"
}

@Composable
fun AuthNavigation(
    navController: NavHostController,
    onAuthSuccess: () -> Unit
) {
    NavHost(navController = navController, startDestination = AuthDestinations.SPLASH_ROUTE) {
        composable(AuthDestinations.SPLASH_ROUTE) {
            SplashScreen(
                onLoadingComplete = {
                    navController.navigate(AuthDestinations.GETTING_STARTED_ROUTE) {
                        popUpTo(AuthDestinations.SPLASH_ROUTE) { inclusive = true }
                    }
                }
            )
        }
        composable(AuthDestinations.GETTING_STARTED_ROUTE) {
            GettingStartedScreen(
                onGetStartedClick = { navController.navigate(AuthDestinations.REGISTER_ROUTE) },
                onSignInClick = { navController.navigate(AuthDestinations.LOGIN_ROUTE) }
            )
        }
        composable(AuthDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onSignInClick = { email, password ->
                    // TODO: Replace with real sign-in logic
                    // If successful:
                    onAuthSuccess()
                },
                onForgotPasswordClick = {
                    // TODO: Implement forgot password logic
                },
                onSignInWithGoogleClick = {
                    // TODO: Implement Google Sign-In
                },
                onSignInWithFacebookClick = {
                    // TODO: Implement Facebook Sign-In
                },
                onSignUpClick = {
                    navController.navigate(AuthDestinations.REGISTER_ROUTE)
                }
            )
        }
        composable(AuthDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                onSignUpClick = { email, password ->
                    // TODO: Replace with real sign-up logic
                    navController.navigate(AuthDestinations.ADD_CARD_DETAILS_ROUTE)
                },
                onSignInClick = {
                    navController.navigate(AuthDestinations.LOGIN_ROUTE) {
                        popUpTo(AuthDestinations.GETTING_STARTED_ROUTE)
                    }
                }
            )
        }
        composable(AuthDestinations.ADD_CARD_DETAILS_ROUTE) {
            AddCardDetailsScreen(
                onContinueClick = { cardType, cardNumber, expiryDate, cvv ->
                    // TODO: Save card logic
                    onAuthSuccess()
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Nếu muốn thêm màn hình main content vào trong graph này
        // composable(AuthDestinations.MAIN_CONTENT_ROUTE) {
        //     MainContentScreen() // hoặc gọi navController.navigate("home")
        // }
    }
}
