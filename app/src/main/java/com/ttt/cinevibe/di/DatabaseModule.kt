package com.ttt.cinevibe.di

import android.content.Context
import androidx.room.Room
import com.ttt.cinevibe.data.local.CineVibeDatabase
import com.ttt.cinevibe.data.local.dao.FavoriteMovieDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCineVibeDatabase(@ApplicationContext context: Context): CineVibeDatabase {
        return Room.databaseBuilder(
            context,
            CineVibeDatabase::class.java,
            "cinevibe_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFavoriteMovieDao(database: CineVibeDatabase): FavoriteMovieDao {
        return database.favoriteMovieDao()
    }
}