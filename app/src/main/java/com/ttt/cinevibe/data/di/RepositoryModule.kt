package com.ttt.cinevibe.data.di

import com.ttt.cinevibe.data.repository.MovieRepositoryImpl
import com.ttt.cinevibe.data.repository.TvSeriesRepositoryImpl
import com.ttt.cinevibe.data.repository.FavoriteMovieRepositoryImpl
import com.ttt.cinevibe.domain.repository.MovieRepository
import com.ttt.cinevibe.domain.repository.TvSeriesRepository
import com.ttt.cinevibe.domain.repository.FavoriteMovieRepository
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
    abstract fun bindTvSeriesRepository(
        tvSeriesRepositoryImpl: TvSeriesRepositoryImpl
    ): TvSeriesRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteMovieRepository(
        favoriteMovieRepositoryImpl: FavoriteMovieRepositoryImpl
    ): FavoriteMovieRepository
} 