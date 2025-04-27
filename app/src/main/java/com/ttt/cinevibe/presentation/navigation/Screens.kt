package com.ttt.cinevibe.presentation.navigation

object Screens {
    const val HOME_ROUTE = "home"
    const val NEW_HOT_ROUTE = "new_hot"
    const val SEARCH_ROUTE = "search"
    const val DOWNLOADS_ROUTE = "downloads"
    const val PROFILE_ROUTE = "profile"
    const val MOVIE_DETAIL_ROUTE = "movie_detail"
    const val MOVIE_DETAIL_ARG = "movieId"
    const val MY_LIST_ROUTE = "my_list"
    
    // Route with arguments
    fun movieDetailRoute(movieId: String): String = "$MOVIE_DETAIL_ROUTE/$movieId"
}