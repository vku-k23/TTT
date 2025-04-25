package com.ttt.cinevibe.presentation.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.ttt.cinevibe.ui.theme.CineVibeTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.presentation.NavDestinations
import com.ttt.cinevibe.presentation.auth.AUTH_GRAPH_ROUTE
import com.ttt.cinevibe.presentation.auth.authNavGraph
import com.ttt.cinevibe.presentation.detail.MovieDetailScreen
import com.ttt.cinevibe.presentation.home.HomeScreen
import com.ttt.cinevibe.presentation.main.MainScreen
import com.ttt.cinevibe.presentation.splash.SplashScreen

// Define a constant for the splash route
private const val SPLASH_ROUTE = "splash"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            CineVibeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CineVibeApp()
                }
            }
        }
    }
}

@Composable
fun CineVibeApp() {
    val navController = rememberNavController()
    
    // Start with the splash screen route
    NavHost(navController = navController, startDestination = SPLASH_ROUTE) {
        // Splash screen
        composable(route = SPLASH_ROUTE) {
            SplashScreen(
                onSplashFinished = {
                    // Navigate to auth flow when splash is done and user is NOT logged in
                    navController.navigate(AUTH_GRAPH_ROUTE) {
                        popUpTo(SPLASH_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    // Navigate directly to main flow if user is already logged in
                    navController.navigate(NavDestinations.MAIN_FLOW) {
                        popUpTo(SPLASH_ROUTE) { inclusive = true }
                    }
                }
            )
        }
        
        // Firebase Auth flow using our new components
        authNavGraph(
            navController = navController,
            onAuthSuccess = {
                // Navigate to main flow after successful authentication
                navController.navigate(NavDestinations.MAIN_FLOW) {
                    popUpTo(AUTH_GRAPH_ROUTE) { inclusive = true }
                }
            }
        )
        
        // Main app flow
        navigation(
            startDestination = NavDestinations.HOME_ROUTE, 
            route = NavDestinations.MAIN_FLOW
        ) {
            composable(NavDestinations.HOME_ROUTE) {
                MainScreen(rootNavController = navController)
            }
            
            composable(
                route = NavDestinations.MOVIE_DETAIL_WITH_ARGS,
                arguments = listOf(
                    navArgument("movieId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: -1
                
                val dummyMovie = Movie(
                    id = movieId,
                    title = "Movie #$movieId",
                    overview = "This is a sample movie description. In a real application, you would fetch this data from the API based on the movie ID.",
                    posterPath = null,
                    backdropPath = null,
                    releaseDate = "2025-04-23",
                    voteAverage = 4.5
                )
                
                MovieDetailScreen(
                    movie = dummyMovie,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
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
