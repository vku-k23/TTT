package com.ttt.cinevibe.presentation.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

const val AUTH_GRAPH_ROUTE = "auth_graph"
const val LOGIN_ROUTE = "login"
const val REGISTER_ROUTE = "register"
const val FORGOT_PASSWORD_ROUTE = "forgot_password"

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    onAuthSuccess: () -> Unit
) {
    navigation(
        startDestination = LOGIN_ROUTE,
        route = AUTH_GRAPH_ROUTE
    ) {
        composable(LOGIN_ROUTE) {
            LoginScreen(
                navController = navController,
                onLoginSuccess = onAuthSuccess,
                onNavigateToRegister = {
                    navController.navigate(REGISTER_ROUTE) {
                        popUpTo(LOGIN_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(FORGOT_PASSWORD_ROUTE)
                }
            )
        }
        
        composable(REGISTER_ROUTE) {
            RegisterScreen(
                navController = navController,
                onRegisterSuccess = onAuthSuccess,
                onNavigateToLogin = {
                    navController.navigate(LOGIN_ROUTE) {
                        popUpTo(REGISTER_ROUTE) { inclusive = true }
                    }
                }
            )
        }
        
        composable(FORGOT_PASSWORD_ROUTE) {
            ForgotPasswordScreen(
                navController = navController,
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}