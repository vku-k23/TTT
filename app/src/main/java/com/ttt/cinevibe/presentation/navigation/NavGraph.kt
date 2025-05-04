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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.ttt.cinevibe.presentation.auth.AUTH_GRAPH_ROUTE
import com.ttt.cinevibe.presentation.detail.MovieDetailScreen
import com.ttt.cinevibe.presentation.detail.MovieDetailViewModel
import com.ttt.cinevibe.presentation.feed.FeedScreen
import com.ttt.cinevibe.presentation.home.HomeScreen
import com.ttt.cinevibe.presentation.mylist.MyListScreen
import com.ttt.cinevibe.presentation.notifications.NotificationsScreen
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
    // Use the combined navigation state that tracks both nav bars
    val navigationState = rememberNavigationState()
    
    // Launch effect to update bottom nav selection when navigation occurs
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    
    LaunchedEffect(currentBackStackEntry) {
        // Only update bottom nav selection for main routes (not detail screens)
        currentBackStackEntry?.destination?.route?.let { currentRoute ->
            if (currentRoute == Screens.HOME_ROUTE || 
                currentRoute == Screens.FEED_ROUTE ||
                currentRoute == Screens.SEARCH_ROUTE ||
                currentRoute == Screens.NOTIFICATIONS_ROUTE ||
                currentRoute == Screens.PROFILE_ROUTE) {
                navigationState.bottomNavRoute.value = currentRoute
            }
            
            // If we're on My List route, update top nav tab
            if (currentRoute == Screens.MY_LIST_ROUTE) {
                navigationState.topNavTab.value = TopNavigationTab.MY_LIST
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screens.HOME_ROUTE
    ) {
        // Home screen
        composable(route = Screens.HOME_ROUTE) {
            HomeScreen(
                selectedTab = navigationState.topNavTab.value,
                onTabSelected = { tab ->
                    navigationState.topNavTab.value = tab
                    // If My List tab is selected, navigate to that screen
                    if (tab == TopNavigationTab.MY_LIST) {
                        navController.navigate(Screens.MY_LIST_ROUTE) {
                            // Pop up to home to avoid building a large backstack
                            popUpTo(Screens.HOME_ROUTE) { inclusive = false }
                        }
                    }
                },
                onMovieClick = { movie ->
                    navController.navigate(Screens.movieDetailRoute(movie.id.toString()))
                },
                onNavigateToDetails = { movie ->
                    navController.navigate(Screens.movieDetailRoute(movie.id.toString()))
                }
            )
        }
        
        // Feed screen (replaced New & Hot)
        composable(route = Screens.FEED_ROUTE) {
            FeedScreen(
                onMovieClick = { movieId ->
                    navController.navigate(Screens.movieDetailRoute(movieId.toString()))
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
        
        // Notifications screen (replaced Downloads)
        composable(route = Screens.NOTIFICATIONS_ROUTE) {
            NotificationsScreen()
        }
        
        // My List screen
        composable(route = Screens.MY_LIST_ROUTE) {
            // Ensure we maintain the bottom nav selection even when on My List
            MyListScreen(
                onBackClick = {
                    // When going back, keep the HOME route selected in bottom nav
                    navigationState.bottomNavRoute.value = Screens.HOME_ROUTE
                    navController.popBackStack(Screens.HOME_ROUTE, false)
                },
                onMovieClick = { movie ->
                    navController.navigate(Screens.movieDetailRoute(movie.id.toString()))
                },
                selectedTab = navigationState.topNavTab.value,
                onTabSelected = { tab ->
                    navigationState.topNavTab.value = tab
                    if (tab != TopNavigationTab.MY_LIST) {
                        // Navigate back to home if another tab is selected
                        navController.popBackStack(Screens.HOME_ROUTE, false)
                    }
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
                },
                onNavigateToDetails = { similarMovieId ->
                    // Navigate to the detail screen of the similar movie
                    navController.navigate(Screens.movieDetailRoute(similarMovieId.toString()))
                }
            )
        }
    }
}