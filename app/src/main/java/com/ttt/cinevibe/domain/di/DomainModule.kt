package com.ttt.cinevibe.domain.di

import com.ttt.cinevibe.data.repository.MovieRepositoryImpl
import com.ttt.cinevibe.domain.repository.MovieRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideMovieRepository(movieRepositoryImpl: MovieRepositoryImpl): MovieRepository {
        return movieRepositoryImpl
    }
}