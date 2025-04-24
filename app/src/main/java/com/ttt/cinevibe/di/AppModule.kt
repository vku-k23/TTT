package com.ttt.cinevibe.di

import android.app.Application
import com.ttt.cinevibe.data.manager.LocalUserManagerImpl
import com.ttt.cinevibe.domain.manager.LocalUserManager
import com.ttt.cinevibe.domain.repository.MovieRepository
import com.ttt.cinevibe.domain.usecases.app_entry.AppEntryUseCases
import com.ttt.cinevibe.domain.usecases.app_entry.ReadAppEntry
import com.ttt.cinevibe.domain.usecases.app_entry.SaveAppEntry
import com.ttt.cinevibe.domain.usecases.movies.GetPopularMoviesUseCase
import com.ttt.cinevibe.domain.usecases.movies.SearchMoviesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideLocalUserManager(
        application: Application
    ): LocalUserManager = LocalUserManagerImpl(application)

    @Provides
    @Singleton
    fun provideAppEntryUseCases(
        localUserManager: LocalUserManager
    ) = AppEntryUseCases(
        saveAppEntry = SaveAppEntry(localUserManager),
        readAppEntry = ReadAppEntry(localUserManager)
    )

    @Provides
    @Singleton
    fun provideGetPopularMoviesUseCase(
        movieRepository: MovieRepository
    ): GetPopularMoviesUseCase {
        return GetPopularMoviesUseCase(movieRepository)
    }

    @Provides
    @Singleton
    fun provideSearchMoviesUseCase(
        movieRepository: MovieRepository
    ): SearchMoviesUseCase {
        return SearchMoviesUseCase(movieRepository)
    }
}