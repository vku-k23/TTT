package com.ttt.cinevibe.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.ttt.cinevibe.presentation.NavDestinations
import com.ttt.cinevibe.presentation.auth.AUTH_GRAPH_ROUTE
import com.ttt.cinevibe.presentation.detail.MovieDetailScreen
import com.ttt.cinevibe.presentation.feed.FeedScreen
import com.ttt.cinevibe.presentation.home.HomeScreen
import com.ttt.cinevibe.presentation.mylist.MyListScreen
import com.ttt.cinevibe.presentation.notifications.NotificationsScreen
import com.ttt.cinevibe.presentation.profile.profileNavGraph
import com.ttt.cinevibe.presentation.reviews.MovieReviewsScreen
import com.ttt.cinevibe.presentation.reviews.UserReviewsScreen
import com.ttt.cinevibe.presentation.search.SearchScreen
import com.ttt.cinevibe.presentation.userProfile.connections.FollowersScreen
import com.ttt.cinevibe.presentation.userProfile.connections.FollowingScreen
import com.ttt.cinevibe.presentation.userProfile.connections.PendingRequestsScreen
import com.ttt.cinevibe.presentation.userProfile.discover.UserRecommendationScreen
import com.ttt.cinevibe.presentation.userProfile.profile.UserProfileScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
                onNavigateToMovieDetails = { movieId ->
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
            NotificationsScreen(
                onNavigateToProfile = { userId ->
                    navController.navigate(Screens.userProfileRoute(userId))
                }
            )
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
            
            MovieDetailScreen(
                movieId = movieId,
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToDetails = { similarMovieId ->
                    navController.navigate(Screens.movieDetailRoute(similarMovieId.toString()))
                },
                onNavigateToReviews = { movieId, movieTitle ->
                    // Navigate to movie reviews screen with movie ID and title
                    val encodedTitle = URLEncoder.encode(movieTitle, StandardCharsets.UTF_8.toString())
                    navController.navigate(Screens.movieReviewsRoute(movieId, movieTitle))
                }
            )
        }
        
        // User recommendations screen
        composable(route = Screens.USER_RECOMMENDATIONS_ROUTE) {
            UserRecommendationScreen(
                onUserClick = { userId ->
                    navController.navigate(Screens.userProfileRoute(userId))
                }
            )
        }
        
        // User profile screen with updated navigation to followers/following
        composable(
            route = "${Screens.USER_PROFILE_ROUTE}/{${Screens.USER_ID_ARG}}",
            arguments = listOf(navArgument(Screens.USER_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(Screens.USER_ID_ARG) ?: ""
            
            UserProfileScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToFollowers = { uid ->
                    navController.navigate(Screens.followersRoute(uid))
                },
                onNavigateToFollowing = { uid ->
                    navController.navigate(Screens.followingRoute(uid))
                },
                onNavigateToPendingRequests = { uid ->
                    navController.navigate(Screens.pendingRequestsRoute(uid))
                },
                onShareProfile = { targetUserId ->
                    // Handle sharing user profile
                },
                onMessageUser = { targetUserId ->
                    // Navigate to chat/messaging in future implementation
                }
            )
        }
        
        // Followers screen
        composable(
            route = "${Screens.FOLLOWERS_ROUTE}/{${Screens.USER_ID_ARG}}",
            arguments = listOf(navArgument(Screens.USER_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(Screens.USER_ID_ARG) ?: ""
            
            FollowersScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProfile = { followerUserId ->
                    navController.navigate(Screens.userProfileRoute(followerUserId))
                }
            )
        }
        
        // Following screen
        composable(
            route = "${Screens.FOLLOWING_ROUTE}/{${Screens.USER_ID_ARG}}",
            arguments = listOf(navArgument(Screens.USER_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(Screens.USER_ID_ARG) ?: ""
            
            FollowingScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProfile = { followingUserId ->
                    navController.navigate(Screens.userProfileRoute(followingUserId))
                }
            )
        }
        
        // Pending Requests screen
        composable(
            route = "${Screens.PENDING_REQUESTS_ROUTE}/{${Screens.USER_ID_ARG}}",
            arguments = listOf(navArgument(Screens.USER_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(Screens.USER_ID_ARG) ?: ""
            
            PendingRequestsScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProfile = { requestUserId ->
                    navController.navigate(Screens.userProfileRoute(requestUserId))
                }
            )
        }
        
        // Movie reviews screen
        composable(
            route = "${NavDestinations.MOVIE_REVIEWS_ROUTE}/{movieId}/{movieTitle}",
            arguments = listOf(
                navArgument("movieId") { type = NavType.LongType },
                navArgument("movieTitle") { 
                    type = NavType.StringType 
                    // Allow special characters in movie titles
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getLong("movieId") ?: 0L
            val encodedTitle = backStackEntry.arguments?.getString("movieTitle") ?: ""
            val movieTitle = URLDecoder.decode(encodedTitle, StandardCharsets.UTF_8.toString())
            
            MovieReviewsScreen(
                movieId = movieId,
                movieTitle = movieTitle,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // User reviews screen
        composable(route = NavDestinations.USER_REVIEWS_ROUTE) {
            UserReviewsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMovie = { movieId ->
                    navController.navigate(Screens.movieDetailRoute(movieId.toString()))
                }
            )
        }
    }
}