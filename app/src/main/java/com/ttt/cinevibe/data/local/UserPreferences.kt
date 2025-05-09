package com.ttt.cinevibe.data.local

import android.content.Context
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
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val DISPLAY_NAME_KEY = stringPreferencesKey("display_name")
        private val EMAIL_KEY = stringPreferencesKey("email")
    }

    /**
     * Save the username to preferences
     */
    suspend fun saveUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
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
     * Clear all user preferences
     */
    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.remove(USERNAME_KEY)
            preferences.remove(DISPLAY_NAME_KEY)
            preferences.remove(EMAIL_KEY)
        }
    }
} 