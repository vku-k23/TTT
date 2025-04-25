package com.ttt.cinevibe.data.manager

import kotlinx.coroutines.flow.Flow
import java.util.Locale

/**
 * Interface for managing the app's language settings
 */
interface LanguageManager {
    /**
     * Get the current app language
     */
    fun getAppLanguage(): Flow<Locale>
    
    /**
     * Set the app language
     * @param languageCode ISO 639-1 code (e.g. "en", "es", "fr")
     */
    suspend fun setAppLanguage(languageCode: String)
    
    /**
     * Get available languages with their display names and language codes
     * @return Map of language display name to language code
     */
    fun getAvailableLanguages(): Map<String, String>
    
    /**
     * Get language name from language code
     * @param languageCode ISO 639-1 code
     * @return Display name of the language in its native form
     */
    fun getLanguageNameFromCode(languageCode: String): String
    
    /**
     * Get language code from language name
     * @param languageName Display name of the language
     * @return ISO 639-1 code
     */
    fun getLanguageCodeFromName(languageName: String): String
}