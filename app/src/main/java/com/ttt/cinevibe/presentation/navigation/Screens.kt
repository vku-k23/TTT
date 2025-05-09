package com.ttt.cinevibe.presentation.navigation

object Screens {
    const val HOME_ROUTE = "home"
    const val FEED_ROUTE = "feed" // Changed from NEW_HOT_ROUTE
    const val SEARCH_ROUTE = "search"
    const val NOTIFICATIONS_ROUTE = "notifications" // Changed from DOWNLOADS_ROUTE
    const val PROFILE_ROUTE = "profile"
    const val MOVIE_DETAIL_ROUTE = "movie_detail"
    const val MOVIE_DETAIL_ARG = "movieId"
    const val MY_LIST_ROUTE = "my_list"
    
    // User recommendations and profiles
    const val USER_RECOMMENDATIONS_ROUTE = "user_recommendations"
    const val USER_PROFILE_ROUTE = "user_profile"
    const val USER_ID_ARG = "userId"
    
    // Route with arguments
    fun movieDetailRoute(movieId: String): String = "$MOVIE_DETAIL_ROUTE/$movieId"
    fun userProfileRoute(userId: String): String = "$USER_PROFILE_ROUTE/$userId"
}