package com.ttt.cinevibe.data.di

import com.ttt.cinevibe.data.repository.*
import com.ttt.cinevibe.domain.repository.CommentRepository
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
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: FirebaseAuthRepositoryImpl
    ): AuthRepository
    
    @Binds
    @Singleton
    abstract fun bindUserRecommendationRepository(
        userRecommendationRepositoryImpl: UserRecommendationRepositoryImpl
    ): UserRecommendationRepository
      @Binds
    @Singleton
    abstract fun bindUserConnectionRepository(
        userConnectionRepositoryImpl: UserConnectionRepositoryImpl
    ): UserConnectionRepository
    
    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        commentRepositoryImpl: CommentRepositoryImpl
    ): CommentRepository
}