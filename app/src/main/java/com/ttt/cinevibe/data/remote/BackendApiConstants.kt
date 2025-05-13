package com.ttt.cinevibe.data.remote

object BackendApiConstants {
    // Replace with your actual backend URL
//    const val BASE_URL = "http://catcosy.shop:8081/"
    const val BASE_URL = "http://192.168.1.4:8081/"
    
    // API endpoints
    const val USER_ENDPOINT = "api/user"
    const val ME_ENDPOINT = "api/user/me"
    const val SYNC_USER_ENDPOINT = "api/user/sync"  // Fixed to match backend endpoint
    const val PROFILE_ENDPOINT = "api/user/profile"
    
    // Auth header for Firebase JWT
    const val AUTH_HEADER = "Authorization"
    const val AUTH_BEARER_PREFIX = "Bearer "
}