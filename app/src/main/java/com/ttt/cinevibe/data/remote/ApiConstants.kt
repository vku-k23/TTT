package com.ttt.cinevibe.data.remote

object ApiConstants {
    // Replace this with your actual TMDb API key
    const val API_KEY = "3fd2be6f0c70a2a598f084ddfb75487c"  // Replace with your actual API key
    const val BASE_URL = "https://api.themoviedb.org/3/"
    const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
    const val POSTER_SIZE = "w500"
    const val BACKDROP_SIZE = "w1280"
    
    // Common query parameters
    const val PARAM_API_KEY = "api_key"
    const val PARAM_LANGUAGE = "language"
    const val DEFAULT_LANGUAGE = "en-US"
    const val PARAM_PAGE = "page"
    const val DEFAULT_PAGE = "1"
}