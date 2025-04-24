package com.ttt.cinevibe.presentation.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ttt.cinevibe.presentation.auth.AuthNavigation
import com.ttt.cinevibe.presentation.home.HomeScreen
import com.ttt.cinevibe.ui.theme.CineVibeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            CineVibeTheme {
                NavHost(navController = navController, startDestination = "auth") {
                    composable("auth") {
                        AuthNavigation(
                            navController = navController,
                            onAuthSuccess = {
                                navController.navigate("home") {
                                    popUpTo("auth") { inclusive = true } // Xoá toàn bộ auth khỏi backstack
                                }
                            }
                        )
                    }

                    composable("home") {
                        HomeScreen(navController)
                    }
                }
            }
        }

    }
}

@Composable
fun CineVibeApp() {
    val navController = rememberNavController()

    CineVibeTheme {
        // Optional: Scaffold nếu bạn cần TopBar/BottomBar
        NavHost(
            navController = navController,
            startDestination = "auth_flow"
        ) {
            composable("auth_flow") {
                AuthNavigation(navController = navController)
            }
        }
    }
}
