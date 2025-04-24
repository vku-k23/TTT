package com.ttt.cinevibe.data.remote

object ApiConstants {
    // Use a single API key throughout the app
    const val API_KEY = "1ee82cdfe968c67bdb6c59307e933537"  // TMDB API key
    const val BASE_URL = "https://api.themoviedb.org/3/"
    const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
    const val POSTER_SIZE = "w500"
    const val BACKDROP_SIZE = "w1280"
    
    // Common query parameters
    const val PARAM_LANGUAGE = "language"
    const val DEFAULT_LANGUAGE = "en-US"
    const val PARAM_PAGE = "page"
    const val DEFAULT_PAGE = "1"
}