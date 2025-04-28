package com.ttt.cinevibe.data.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.ttt.cinevibe.data.remote.ApiConstants
import com.ttt.cinevibe.data.remote.BackendApiConstants
import com.ttt.cinevibe.data.remote.MovieApi
import com.ttt.cinevibe.data.remote.api.MovieApiService
import com.ttt.cinevibe.data.remote.api.UserApiService
import com.ttt.cinevibe.data.remote.interceptor.AuthInterceptor
import com.ttt.cinevibe.data.remote.interceptor.FirebaseTokenInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TMDBRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackendRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideBackendOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        firebaseTokenInterceptor: FirebaseTokenInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(firebaseTokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideJson() = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    @Provides
    @Singleton
    @TMDBRetrofit
    fun provideTMDBRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
    
    @Provides
    @Singleton
    @BackendRetrofit
    fun provideBackendRetrofit(
        @BackendOkHttpClient backendOkHttpClient: OkHttpClient, 
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BackendApiConstants.BASE_URL)
            .client(backendOkHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideMovieApi(@TMDBRetrofit retrofit: Retrofit): MovieApi {
        return retrofit.create(MovieApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideMovieApiService(@TMDBRetrofit retrofit: Retrofit): MovieApiService {
        return retrofit.create(MovieApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideUserApiService(@BackendRetrofit retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }
    
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class BackendOkHttpClient
    
    @Provides
    @Singleton
    @BackendOkHttpClient
    fun provideBackendOkHttpClientWithQualifier(
        loggingInterceptor: HttpLoggingInterceptor,
        firebaseTokenInterceptor: FirebaseTokenInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(firebaseTokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}