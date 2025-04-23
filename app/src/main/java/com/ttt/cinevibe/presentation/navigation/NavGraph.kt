package com.ttt.cinevibe.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ttt.cinevibe.presentation.details.MovieDetailScreen
import com.ttt.cinevibe.presentation.downloads.DownloadsScreen
import com.ttt.cinevibe.presentation.home.HomeScreen
import com.ttt.cinevibe.presentation.newhot.NewHotScreen
import com.ttt.cinevibe.presentation.newhot.NewHotViewModel
import com.ttt.cinevibe.presentation.profile.ProfileScreen
import com.ttt.cinevibe.presentation.search.SearchScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screens.HOME_ROUTE
    ) {
        composable(route = Screens.HOME_ROUTE) {
            HomeScreen(navController = navController)
        }
        
        composable(route = Screens.NEW_HOT_ROUTE) {
            val viewModel = hiltViewModel<NewHotViewModel>()
            NewHotScreen(
                navController = navController,
                comingSoonMovies = viewModel.getComingSoonMovies(),
                everyoneWatchingMovies = viewModel.getEveryoneWatchingMovies()
            )
        }
        
        composable(route = Screens.SEARCH_ROUTE) {
            SearchScreen(navController = navController)
        }
        
        composable(route = Screens.DOWNLOADS_ROUTE) {
            DownloadsScreen(navController = navController)
        }
        
        composable(route = Screens.PROFILE_ROUTE) {
            ProfileScreen(navController = navController)
        }
        
        // Detail screen with movie ID parameter
        composable(
            route = "${Screens.MOVIE_DETAIL_ROUTE}/{${Screens.MOVIE_DETAIL_ARG}}",
            arguments = listOf(navArgument(Screens.MOVIE_DETAIL_ARG) { type = NavType.IntType })
        ) {
            val movieId = it.arguments?.getInt(Screens.MOVIE_DETAIL_ARG) ?: 0
            MovieDetailScreen(
                navController = navController,
                movieId = movieId
            )
        }
    }
}