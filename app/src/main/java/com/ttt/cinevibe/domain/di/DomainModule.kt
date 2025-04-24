package com.ttt.cinevibe.domain.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    // Domain level dependencies can be added here if needed
    // Removing duplicate MovieRepository binding
}