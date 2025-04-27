package com.ttt.cinevibe.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ttt.cinevibe.presentation.auth.AUTH_GRAPH_ROUTE
import com.ttt.cinevibe.presentation.detail.MovieDetailScreen
import com.ttt.cinevibe.presentation.detail.MovieDetailViewModel
import com.ttt.cinevibe.presentation.downloads.DownloadsScreen
import com.ttt.cinevibe.presentation.home.HomeScreen
import com.ttt.cinevibe.presentation.mylist.MyListScreen
import com.ttt.cinevibe.presentation.newhot.NewHotScreen
import com.ttt.cinevibe.presentation.newhot.NewHotViewModel
import com.ttt.cinevibe.presentation.profile.PROFILE_GRAPH_ROUTE
import com.ttt.cinevibe.presentation.profile.profileNavGraph
import com.ttt.cinevibe.presentation.search.SearchScreen
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White

@Composable
fun NavGraph(
    navController: NavHostController,
    rootNavController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screens.HOME_ROUTE
    ) {
        // Home screen
        composable(route = Screens.HOME_ROUTE) {
            HomeScreen(
                onMovieClick = { movie ->
                    navController.navigate(Screens.movieDetailRoute(movie.id.toString()))
                },
                onNavigateToDetails = { movie ->
                    navController.navigate(Screens.movieDetailRoute(movie.id.toString()))
                },
                onNavigateToMyList = {
                    navController.navigate(Screens.MY_LIST_ROUTE)
                }
            )
        }
        
        // New & Hot screen
        composable(route = Screens.NEW_HOT_ROUTE) {
            val viewModel = hiltViewModel<NewHotViewModel>()
            NewHotScreen(
                onMovieClick = { movie ->
                    navController.navigate(Screens.movieDetailRoute(movie.id.toString()))
                }
            )
        }
        
        // Search screen
        composable(route = Screens.SEARCH_ROUTE) {
            SearchScreen(
                onMovieClick = { movie ->
                    navController.navigate(Screens.movieDetailRoute(movie.id.toString()))
                }
            )
        }
        
        // Downloads screen
        composable(route = Screens.DOWNLOADS_ROUTE) {
            DownloadsScreen()
        }
        
        // My List screen
        composable(route = Screens.MY_LIST_ROUTE) {
            MyListScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onMovieClick = { movie ->
                    navController.navigate(Screens.movieDetailRoute(movie.id.toString()))
                }
            )
        }
        
        // Use the profile nav graph for profile-related screens
        profileNavGraph(
            navController = navController,
            onLogout = {
                // Use rootNavController instead of navController for navigating to auth flow
                rootNavController.navigate(AUTH_GRAPH_ROUTE) {
                    // Clear the entire back stack so user can't navigate back to the main flow
                    popUpTo(0) { inclusive = true }
                }
            }
        )
        
        // Detail screen with movie ID parameter
        composable(
            route = "${Screens.MOVIE_DETAIL_ROUTE}/{${Screens.MOVIE_DETAIL_ARG}}",
            arguments = listOf(navArgument(Screens.MOVIE_DETAIL_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString(Screens.MOVIE_DETAIL_ARG)?.toIntOrNull() ?: 0
            
            // Simply render the MovieDetailScreen and let it manage its own state
            // This avoids creating two instances of the ViewModel
            MovieDetailScreen(
                movieId = movieId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}