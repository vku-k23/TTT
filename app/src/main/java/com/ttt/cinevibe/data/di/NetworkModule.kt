package com.ttt.cinevibe.data.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.ttt.cinevibe.data.remote.ApiConstants
import com.ttt.cinevibe.data.remote.BackendApiConstants
import com.ttt.cinevibe.data.remote.CloudinaryService
import com.ttt.cinevibe.data.remote.MovieApi
import com.ttt.cinevibe.data.remote.api.MovieApiService
import com.ttt.cinevibe.data.remote.api.MovieReviewApiService
import com.ttt.cinevibe.data.remote.api.UserApiService
import com.ttt.cinevibe.data.remote.api.UserConnectionApiService
import com.ttt.cinevibe.data.remote.api.UserRecommendationApiService
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

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TMDBOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackendOkHttpClient

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
    @TMDBOkHttpClient
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
    @BackendOkHttpClient
    fun provideBackendOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        firebaseTokenInterceptor: FirebaseTokenInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(firebaseTokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS) // Tăng thời gian timeout kết nối
            .readTimeout(30, TimeUnit.SECONDS)    // Tăng thời gian timeout đọc dữ liệu
            .writeTimeout(30, TimeUnit.SECONDS)   // Thêm timeout cho ghi dữ liệu
            .build()
    }
    
    @Provides
    @Singleton
    fun provideCloudinaryOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideJson() = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
        isLenient = true  // Add lenient parsing to handle different JSON formats
        encodeDefaults = true  // Encode default values when serializing
    }

    @Provides
    @Singleton
    @TMDBRetrofit
    fun provideTMDBRetrofit(@TMDBOkHttpClient okHttpClient: OkHttpClient, json: Json): Retrofit {
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
        @BackendOkHttpClient okHttpClient: OkHttpClient, 
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BackendApiConstants.BASE_URL)
            .client(okHttpClient)
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

    @Provides
    @Singleton
    fun provideUserRecommendationApiService(@BackendRetrofit retrofit: Retrofit): UserRecommendationApiService {
        return retrofit.create(UserRecommendationApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideUserConnectionApiService(@BackendRetrofit retrofit: Retrofit): UserConnectionApiService {
        return retrofit.create(UserConnectionApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCloudinaryService(okHttpClient: OkHttpClient): CloudinaryService {
        return CloudinaryService(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideMovieReviewApiService(@BackendRetrofit retrofit: Retrofit): MovieReviewApiService {
        return retrofit.create(MovieReviewApiService::class.java)
    }
}