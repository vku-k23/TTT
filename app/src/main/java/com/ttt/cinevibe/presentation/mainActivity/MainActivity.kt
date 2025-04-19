package com.ttt.cinevibe.presentation.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ttt.cinevibe.ui.theme.CineVibeTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.ttt.cinevibe.presentation.auth.AuthDestinations
import com.ttt.cinevibe.presentation.auth.LoginScreen
import com.ttt.cinevibe.presentation.auth.RegisterScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CineVibeTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "auth_flow") {
                    navigation(startDestination = AuthDestinations.LOGIN_ROUTE, route = "auth_flow") {
                        composable(AuthDestinations.LOGIN_ROUTE) {
                            LoginScreen(
                                onLoginClick = { email, password ->
                                    // TODO: Handle login logic and navigate to main app
                                },
                                onRegisterClick = {
                                    navController.navigate(AuthDestinations.REGISTER_ROUTE)
                                }
                            )
                        }
                        composable(AuthDestinations.REGISTER_ROUTE) {
                            RegisterScreen(
                                onRegisterClick = { email, password ->
                                    // TODO: Handle registration logic and navigate to main app
                                },
                                onLoginClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                    // TODO: Add main app navigation destinations here
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CineVibeTheme {
        Greeting("Android")
    }
}