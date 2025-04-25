package com.ttt.cinevibe.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
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
        // For Android N and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Use AppCompatDelegate for setting locale (recommended approach)
            val localeList = LocaleListCompat.create(locale)
            AppCompatDelegate.setApplicationLocales(localeList)
            
            // Update configuration for immediate effect
            val configuration = context.resources.configuration
            configuration.setLocale(locale)
            return context.createConfigurationContext(configuration)
        } else {
            // For older Android versions
            val configuration = Configuration(context.resources.configuration)
            configuration.locale = locale
            
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(
                configuration,
                context.resources.displayMetrics
            )
            
            return context
        }
    }
}