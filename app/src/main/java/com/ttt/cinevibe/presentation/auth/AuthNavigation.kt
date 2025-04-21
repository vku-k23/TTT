package com.ttt.cinevibe.presentation.auth

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

object AuthDestinations {
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
}

@Composable
fun AuthNavGraph(
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = AuthDestinations.LOGIN_ROUTE) {
        composable(AuthDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onLoginClick = { email, password ->
                    // TODO: Handle login logic
                },
                onRegisterClick = {
                    navController.navigate(AuthDestinations.REGISTER_ROUTE)
                }
            )
        }
        composable(AuthDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                onRegisterClick = { email, password ->
                    // TODO: Handle registration logic
                },
                onLoginClick = {
                    navController.popBackStack() // Go back to login
                }
            )
        }
    }
}