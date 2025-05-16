package com.ttt.cinevibe.presentation

/**
 * Contains all navigation destinations used in the app
 */
object NavDestinations {
    // Auth flow
    const val AUTH_FLOW = "auth_flow"
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    
    // Main flow
    const val MAIN_FLOW = "main_flow"
    const val HOME_ROUTE = "home"
    const val MOVIE_DETAIL_ROUTE = "movie_detail"
    const val MOVIE_DETAIL_WITH_ARGS = "$MOVIE_DETAIL_ROUTE/{movieId}"
    
    // User profile and connections
    const val USER_PROFILE_ROUTE = "user_profile"
    const val USER_PROFILE_WITH_ARGS = "$USER_PROFILE_ROUTE/{userId}"
    const val FOLLOWERS_ROUTE = "followers" 
    const val FOLLOWERS_WITH_ARGS = "$FOLLOWERS_ROUTE/{userId}"
    const val FOLLOWING_ROUTE = "following"
    const val FOLLOWING_WITH_ARGS = "$FOLLOWING_ROUTE/{userId}"
    const val NOTIFICATIONS_ROUTE = "notifications"
    
    // Movie reviews
    const val MOVIE_REVIEWS_ROUTE = "movie_reviews"
    const val MOVIE_REVIEWS_WITH_ARGS = "$MOVIE_REVIEWS_ROUTE/{movieId}/{movieTitle}"
    const val USER_REVIEWS_ROUTE = "user_reviews"
    const val USER_REVIEWS_WITH_ARGS = "$USER_REVIEWS_ROUTE/{userId}"
}