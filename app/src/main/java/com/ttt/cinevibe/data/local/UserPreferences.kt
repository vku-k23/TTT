package com.ttt.cinevibe.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * DataStore implementation for storing user related preferences
 */
@Singleton
class UserPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val TAG = "UserPreferences"
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val DISPLAY_NAME_KEY = stringPreferencesKey("display_name")
        private val EMAIL_KEY = stringPreferencesKey("email")
        // Add any additional user-related preference keys here
    }

    /**
     * Save the username to preferences
     */
    suspend fun saveUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
            Log.d(TAG, "Username saved: $username")
        }
    }

    /**
     * Get the username from preferences
     */
    suspend fun getUsername(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[USERNAME_KEY]
        }.firstOrNull()
    }

    /**
     * Save the display name to preferences
     */
    suspend fun saveDisplayName(displayName: String) {
        context.dataStore.edit { preferences ->
            preferences[DISPLAY_NAME_KEY] = displayName
            Log.d(TAG, "Display name saved: $displayName")
        }
    }

    /**
     * Get the display name from preferences
     */
    suspend fun getDisplayName(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[DISPLAY_NAME_KEY]
        }.firstOrNull()
    }

    /**
     * Save the email to preferences
     */
    suspend fun saveEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = email
            Log.d(TAG, "Email saved: $email")
        }
    }

    /**
     * Get the email from preferences
     */
    suspend fun getEmail(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[EMAIL_KEY]
        }.firstOrNull()
    }

    /**
     * Clear all user preferences - completely removes all data to prevent issues
     * when switching between users
     */
    suspend fun clearUserData() {
        Log.d(TAG, "Clearing all user preferences data")
        try {
            context.dataStore.edit { preferences ->
                // Remove individual keys for specific handling
                preferences.remove(USERNAME_KEY)
                preferences.remove(DISPLAY_NAME_KEY)
                preferences.remove(EMAIL_KEY)

                // Also completely clear all keys to ensure no data remains
                preferences.clear()
            }
            Log.d(TAG, "User preferences data cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing user preferences data: ${e.message}")
        }
    }
}