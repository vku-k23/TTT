package com.ttt.cinevibe.di

import android.app.Application
import com.ttt.cinevibe.data.manager.LocalUserManagerImpl
import com.ttt.cinevibe.domain.manager.LocalUserManager
import com.ttt.cinevibe.domain.usecases.app_entry.AppEntryUseCases
import com.ttt.cinevibe.domain.usecases.app_entry.ReadAppEntry
import com.ttt.cinevibe.domain.usecases.app_entry.SaveAppEntry
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

    // Add more use cases here

    //db

    //usecase


}