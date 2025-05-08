package com.ttt.cinevibe.di

import android.app.Application
import com.ttt.cinevibe.data.manager.LocalUserManagerImpl
import com.ttt.cinevibe.domain.manager.LocalUserManager
import com.ttt.cinevibe.domain.repository.FavoriteMovieRepository
import com.ttt.cinevibe.domain.repository.MovieRepository
import com.ttt.cinevibe.domain.usecase.app_entry.AppEntryUseCases
import com.ttt.cinevibe.domain.usecase.app_entry.ReadAppEntry
import com.ttt.cinevibe.domain.usecase.app_entry.SaveAppEntry
import com.ttt.cinevibe.domain.usecase.favorites.AddMovieToFavoritesUseCase
import com.ttt.cinevibe.domain.usecase.favorites.FavoriteMoviesUseCases
import com.ttt.cinevibe.domain.usecase.favorites.GetFavoriteMoviesUseCase
import com.ttt.cinevibe.domain.usecase.favorites.IsMovieFavoriteUseCase
import com.ttt.cinevibe.domain.usecase.favorites.RemoveMovieFromFavoritesUseCase
import com.ttt.cinevibe.domain.usecase.movies.GetPopularMoviesUseCase
import com.ttt.cinevibe.domain.usecase.movies.GetTopRatedMoviesUseCase
import com.ttt.cinevibe.domain.usecase.movies.GetTrendingMoviesUseCase
import com.ttt.cinevibe.domain.usecase.movies.GetUpcomingMoviesUseCase
import com.ttt.cinevibe.domain.usecase.movies.SearchMoviesUseCase
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
    fun provideGetTopRatedMoviesUseCase(
        movieRepository: MovieRepository
    ): GetTopRatedMoviesUseCase {
        return GetTopRatedMoviesUseCase(movieRepository)
    }

    @Provides
    @Singleton
    fun provideGetTrendingMoviesUseCase(
        movieRepository: MovieRepository
    ): GetTrendingMoviesUseCase {
        return GetTrendingMoviesUseCase(movieRepository)
    }

    @Provides
    @Singleton
    fun provideGetUpcomingMoviesUseCase(
        movieRepository: MovieRepository
    ): GetUpcomingMoviesUseCase {
        return GetUpcomingMoviesUseCase(movieRepository)
    }

    @Provides
    @Singleton
    fun provideSearchMoviesUseCase(
        movieRepository: MovieRepository
    ): SearchMoviesUseCase {
        return SearchMoviesUseCase(movieRepository)
    }

    @Provides
    @Singleton
    fun provideFavoriteMoviesUseCases(
        favoriteMovieRepository: FavoriteMovieRepository
    ): FavoriteMoviesUseCases {
        return FavoriteMoviesUseCases(
            getFavoriteMovies = GetFavoriteMoviesUseCase(favoriteMovieRepository),
            addMovieToFavorites = AddMovieToFavoritesUseCase(favoriteMovieRepository),
            removeMovieFromFavorites = RemoveMovieFromFavoritesUseCase(favoriteMovieRepository),
            isMovieFavorite = IsMovieFavoriteUseCase(favoriteMovieRepository)
        )
    }
}