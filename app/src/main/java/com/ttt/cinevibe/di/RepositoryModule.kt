package com.ttt.cinevibe.di

import com.ttt.cinevibe.data.repository.FavoriteMovieRepositoryImpl
import com.ttt.cinevibe.data.repository.MovieRepositoryImpl
import com.ttt.cinevibe.data.repository.MovieReviewRepositoryImpl
import com.ttt.cinevibe.domain.repository.FavoriteMovieRepository
import com.ttt.cinevibe.domain.repository.MovieRepository
import com.ttt.cinevibe.domain.repository.MovieReviewRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMovieRepository(
        movieRepositoryImpl: MovieRepositoryImpl
    ): MovieRepository
    
    @Binds
    @Singleton
    abstract fun bindFavoriteMovieRepository(
        favoriteMovieRepositoryImpl: FavoriteMovieRepositoryImpl
    ): FavoriteMovieRepository

    @Binds
    @Singleton
    abstract fun bindMovieReviewRepository(
        movieReviewRepositoryImpl: MovieReviewRepositoryImpl
    ): MovieReviewRepository
}