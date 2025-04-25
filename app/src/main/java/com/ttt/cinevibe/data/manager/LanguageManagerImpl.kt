package com.ttt.cinevibe.data.manager

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
private val LANGUAGE_KEY = stringPreferencesKey("app_language")

@Singleton
class LanguageManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LanguageManager {

    // Map of language display names to their ISO 639-1 codes
    private val languageMap = mapOf(
        "English" to "en",
        // "Spanish" to "es",
        "French" to "fr",
        // "German" to "de",
        // "Italian" to "it",
        "Japanese" to "ja",
        // "Korean" to "ko",
        // "Mandarin" to "zh",
        // "Hindi" to "hi",
        // "Arabic" to "ar",
        // "Portuguese" to "pt",
        // "Russian" to "ru",
        "Vietnamese" to "vi"
    )

    // Map of ISO 639-1 codes to their display names (reverse of languageMap)
    private val codeToLanguageMap = languageMap.entries.associateBy({ it.value }) { it.key }

    override fun getAppLanguage(): Flow<Locale> {
        return context.dataStore.data.map { preferences ->
            val languageCode = preferences[LANGUAGE_KEY] ?: Locale.getDefault().language
            Locale(languageCode)
        }
    }

    override suspend fun setAppLanguage(languageCode: String) {
        // Update the preference value
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
        
        // Apply the change immediately to ensure it takes effect
        val locale = Locale(languageCode)
        
        // Set JVM default locale (affects string formatting throughout the app)
        Locale.setDefault(locale)
        
        // Update app-wide configuration through AppCompatDelegate
        // This is the main mechanism for applying locale changes in modern Android
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)
        
        // For applications running on all devices, we need an immediate update
        try {
            val resources = context.resources
            val configuration = Configuration(resources.configuration).apply {
                setLocale(locale)
                setLayoutDirection(locale)
            }
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getAvailableLanguages(): Map<String, String> {
        return languageMap
    }

    override fun getLanguageNameFromCode(languageCode: String): String {
        return codeToLanguageMap[languageCode] ?: "English"
    }

    override fun getLanguageCodeFromName(languageName: String): String {
        return languageMap[languageName] ?: "en"
    }
}