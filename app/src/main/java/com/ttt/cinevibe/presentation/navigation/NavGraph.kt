package com.ttt.cinevibe.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.presentation.detail.MovieDetailScreen
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
            HomeScreen(
                onMovieClick = { movie ->
                    navController.navigate(Screens.movieDetailRoute(movie.id.toString()))
                }
            )
        }
        
        composable(route = Screens.NEW_HOT_ROUTE) {
            val viewModel = hiltViewModel<NewHotViewModel>()
            NewHotScreen(
                onMovieClick = { movie ->
                    navController.navigate(Screens.movieDetailRoute(movie.id.toString()))
                }
            )
        }
        
        composable(route = Screens.SEARCH_ROUTE) {
            SearchScreen(
                onMovieClick = { movie ->
                    navController.navigate(Screens.movieDetailRoute(movie.id.toString()))
                }
            )
        }
        
        composable(route = Screens.DOWNLOADS_ROUTE) {
            DownloadsScreen()
        }
        
        composable(route = Screens.PROFILE_ROUTE) {
            ProfileScreen(
                onLogout = {
                    // Navigate back to auth flow when user logs out
                    navController.navigate("auth_graph") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Detail screen with movie ID parameter
        composable(
            route = "${Screens.MOVIE_DETAIL_ROUTE}/{${Screens.MOVIE_DETAIL_ARG}}",
            arguments = listOf(navArgument(Screens.MOVIE_DETAIL_ARG) { type = NavType.IntType })
        ) {
            val movieId = it.arguments?.getInt(Screens.MOVIE_DETAIL_ARG) ?: 0
            
            // Sample dummy movie for testing purposes
            val dummyMovie = Movie(
                id = movieId,
                title = "Movie #$movieId",
                overview = "This is a sample movie description.",
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