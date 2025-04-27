package com.ttt.cinevibe.utils

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * A singleton object that provides consistent locale configuration across the app
 */
object LocaleConfigurationProvider {
    
    // Cache the last applied locale to detect changes
    private var lastAppliedLocale: Locale? = null
    
    /**
     * Creates a new configuration with the specified locale
     */
    fun createConfigurationWithLocale(baseConfig: Configuration, locale: Locale): Configuration {
        return Configuration(baseConfig).apply {
            setLocale(locale)
            setLayoutDirection(locale)
            lastAppliedLocale = locale
        }
    }
    
    /**
     * Applies the locale to a context's resources
     * @return true if a new locale was applied, false if using the same locale as before
     */
    fun applyLocaleToContext(context: Context, locale: Locale): Boolean {
        // Skip if same locale is already applied (optimization)
        val currentLocale = getCurrentLocale(context)
        if (currentLocale != null && 
            currentLocale.language == locale.language && 
            currentLocale.country == locale.country) {
            return false
        }
        
        // 1. Set JVM default locale
        Locale.setDefault(locale)
        
        // 2. Create configuration with new locale
        val config = createConfigurationWithLocale(context.resources.configuration, locale)
        
        // 3. Update the resources
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        
        // Remember this locale
        lastAppliedLocale = locale
        return true
    }
    
    /**
     * Gets the current locale from a context's resources
     */
    fun getCurrentLocale(context: Context): Locale? {
        return ConfigurationCompat.getLocales(context.resources.configuration)[0]
    }
    
    /**
     * Returns the last explicitly applied locale, or null if none has been applied yet
     */
    fun getLastAppliedLocale(): Locale? {
        return lastAppliedLocale
    }
}

/**
 * CompositionLocal to provide the current app locale throughout the composition hierarchy
 */
val LocalAppLocale = compositionLocalOf<Locale> { Locale.getDefault() }