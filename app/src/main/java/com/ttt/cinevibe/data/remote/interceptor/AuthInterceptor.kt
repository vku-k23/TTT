package com.ttt.cinevibe.data.remote.interceptor

import com.ttt.cinevibe.data.remote.ApiConstants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        
        val url = originalUrl.newBuilder()
            .addQueryParameter("api_key", ApiConstants.API_KEY)
            .build()
            
        val request = originalRequest.newBuilder()
            .url(url)
            .build()
            
        return chain.proceed(request)
    }
}