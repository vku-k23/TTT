package com.ttt.cinevibe.presentation.navigation

import com.ttt.cinevibe.presentation.NavDestinations
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// Helper object for screen routes
object Screens {
    // Main screens
    const val HOME_ROUTE = "home"
    const val FEED_ROUTE = "feed"
    const val SEARCH_ROUTE = "search"
    const val NOTIFICATIONS_ROUTE = "notifications"
    const val MY_LIST_ROUTE = "my_list"
    const val PROFILE_ROUTE = "profile"
    
    // Movie detail
    const val MOVIE_DETAIL_ROUTE = "movie_detail"
    const val MOVIE_DETAIL_ARG = "movieId"
    
    // User profile
    const val USER_PROFILE_ROUTE = "user_profile"
    const val USER_ID_ARG = "userId"
    
    // Connection screens
    const val FOLLOWERS_ROUTE = "followers"
    const val FOLLOWING_ROUTE = "following"
    const val PENDING_REQUESTS_ROUTE = "pending_requests"
    
    // Reviews screen
    const val USER_REVIEWS_ROUTE = "user_reviews"
    
    // Recommendation screen
    const val USER_RECOMMENDATIONS_ROUTE = "user_recommendations"
    
    // Helper functions for routes with arguments
    fun movieDetailRoute(movieId: String): String {
        return "$MOVIE_DETAIL_ROUTE/$movieId"
    }
    
    fun userProfileRoute(userId: String): String {
        return "$USER_PROFILE_ROUTE/$userId"
    }
    
    fun followersRoute(userId: String): String {
        return "$FOLLOWERS_ROUTE/$userId"
    }
    
    fun followingRoute(userId: String): String {
        return "$FOLLOWING_ROUTE/$userId"
    }
    
    fun pendingRequestsRoute(userId: String): String {
        return "$PENDING_REQUESTS_ROUTE/$userId"
    }
    
    fun userReviewsRoute(userId: String): String {
        return "$USER_REVIEWS_ROUTE/$userId"
    }
    
    // Movie reviews navigation helpers
    fun movieReviewsRoute(movieId: Long, movieTitle: String): String {
        val encodedTitle = URLEncoder.encode(movieTitle, StandardCharsets.UTF_8.toString())
        return "${NavDestinations.MOVIE_REVIEWS_ROUTE}/$movieId/$encodedTitle"
    }
}