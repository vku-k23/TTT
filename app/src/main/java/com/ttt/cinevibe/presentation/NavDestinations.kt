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
}