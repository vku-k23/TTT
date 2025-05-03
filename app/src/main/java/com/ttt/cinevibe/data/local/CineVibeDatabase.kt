package com.ttt.cinevibe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ttt.cinevibe.data.local.dao.FavoriteMovieDao
import com.ttt.cinevibe.data.local.entity.FavoriteMovieEntity

@Database(
    entities = [FavoriteMovieEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CineVibeDatabase : RoomDatabase() {
    abstract fun favoriteMovieDao(): FavoriteMovieDao
}