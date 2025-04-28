package com.ttt.cinevibe.di

import com.ttt.cinevibe.data.local.PendingSyncManager
import com.ttt.cinevibe.data.local.SharedPrefPendingSyncManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt cung cấp các dependency liên quan đến lưu trữ và xử lý cục bộ
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LocalModule {
    
    /**
     * Cung cấp triển khai cho PendingSyncManager
     */
    @Binds
    @Singleton
    abstract fun bindPendingSyncManager(
        sharedPrefPendingSyncManager: SharedPrefPendingSyncManager
    ): PendingSyncManager
}