package com.ttt.cinevibe.data.di

import com.ttt.cinevibe.data.manager.LanguageManager
import com.ttt.cinevibe.data.manager.LanguageManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    
    @Binds
    @Singleton
    abstract fun bindLanguageManager(
        languageManagerImpl: LanguageManagerImpl
    ): LanguageManager
}