package com.ttt.cinevibe.utils

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * A wrapper component that ensures the provided locale is applied to the context of all child composables
 * Use this wrapper around screens or components that need to maintain the app's language setting
 */
@Composable
fun LanguageContextWrapper(
    locale: Locale,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    // Apply locale to context on each composition
    // This is more aggressive than before to ensure language consistency
    SideEffect {
        LocaleConfigurationProvider.applyLocaleToContext(context, locale)
    }
    
    // Additional effect to handle configuration changes
    LaunchedEffect(locale) {
        Log.d("LanguageContext", "Applying locale: ${locale.language} to context")
        LocaleConfigurationProvider.applyLocaleToContext(context, locale)
    }
    
    // Provide the locale through CompositionLocal so descendants can access it
    CompositionLocalProvider(LocalAppLocale provides locale) {
        // Render the content with the updated locale context
        content()
    }
}