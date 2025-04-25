package com.ttt.cinevibe.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Helper class for setting the app's locale
 */
object LocaleHelper {
    /**
     * Set the app's locale
     * @param context Application context
     * @param locale The locale to apply
     */
    fun setLocale(context: Context, locale: Locale): Context {
        // Step 1: Update JVM default locale - critical for formatting
        Locale.setDefault(locale)
        
        // Step 2: Create and apply the locale list
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)
        
        // Step 3: Create a context with the new locale configuration
        return updateResourcesLegacy(context, locale)
    }
    
    /**
     * Update resources configuration using appropriate method based on API level
     */
    private fun updateResourcesLegacy(context: Context, locale: Locale): Context {
        // For Android N and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val configuration = Configuration(context.resources.configuration).apply {
                setLocale(locale)
                setLayoutDirection(locale)
            }
            
            return context.createConfigurationContext(configuration)
        } 
        // For older Android versions
        else {
            val resources = context.resources
            val configuration = Configuration(resources.configuration).apply {
                // Using the proper setter method instead of direct assignment
                setLocale(locale)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    setLayoutDirection(locale)
                }
            }
            
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
            
            return ContextWrapper(context)
        }
    }
    
    /**
     * Utility method to check if current locale matches system locale
     */
    fun isUsingSystemLocale(context: Context): Boolean {
        val systemLocale = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
        val appLocale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
        return systemLocale?.language == appLocale?.language
    }
}