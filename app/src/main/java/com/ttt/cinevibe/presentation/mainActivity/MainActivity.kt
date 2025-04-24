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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
import com.ttt.cinevibe.presentation.auth.LoginScreen
import com.ttt.cinevibe.presentation.auth.RegisterScreen
import com.ttt.cinevibe.presentation.detail.MovieDetailScreen
import com.ttt.cinevibe.presentation.home.HomeScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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
    
    NavHost(navController = navController, startDestination = NavDestinations.AUTH_FLOW) {
        // Auth flow
        navigation(
            startDestination = NavDestinations.LOGIN_ROUTE, 
            route = NavDestinations.AUTH_FLOW
        ) {
            composable(NavDestinations.LOGIN_ROUTE) {
                LoginScreen(
                    onLoginClick = { _, _ ->
                        // Navigate to main flow after successful login
                        navController.navigate(NavDestinations.MAIN_FLOW) {
                            popUpTo(NavDestinations.AUTH_FLOW) { inclusive = true }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(NavDestinations.REGISTER_ROUTE)
                    }
                )
            }
            composable(NavDestinations.REGISTER_ROUTE) {
                RegisterScreen(
                    onRegisterClick = { _, _ ->
                        // Navigate to main flow after successful registration
                        navController.navigate(NavDestinations.MAIN_FLOW) {
                            popUpTo(NavDestinations.AUTH_FLOW) { inclusive = true }
                        }
                    },
                    onLoginClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        // Main app flow
        navigation(
            startDestination = NavDestinations.HOME_ROUTE, 
            route = NavDestinations.MAIN_FLOW
        ) {
            composable(NavDestinations.HOME_ROUTE) {
                HomeScreen(
                    onMovieClick = { movie ->
                        navController.navigate("${NavDestinations.MOVIE_DETAIL_ROUTE}/${movie.id}")
                    }
                )
            }
            
            composable(
                route = NavDestinations.MOVIE_DETAIL_WITH_ARGS,
                arguments = listOf(
                    navArgument("movieId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                // In a real app, you would fetch movie details using the ID
                // For now, we'll use a temporary movie object
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: -1
                
                // This is a workaround for the demo - in a real app, use a ViewModel 
                // to fetch movie details by ID from the repository
                val dummyMovie = Movie(
                    id = movieId,
                    title = "Movie #$movieId",
                    overview = "This is a sample movie description. In a real application, you would fetch this data from the API based on the movie ID.",
                    posterPath = null,
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