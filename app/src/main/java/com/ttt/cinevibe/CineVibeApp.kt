package com.ttt.cinevibe

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.ttt.cinevibe.data.manager.LanguageManager
import com.ttt.cinevibe.utils.LocaleHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class CineVibeApp : Application() {

    @Inject
    lateinit var languageManager: LanguageManager
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    companion object {
        private const val TAG = "CineVibeApp"
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize with stored language immediately
        initializeAppLanguage()
        
        // Set up language observer for changes during app lifecycle
        observeLanguageChanges()
        
        // Register for configuration changes
        registerActivityLifecycleCallbacks(LocaleActivityLifecycleCallbacks(this))
        
        // Set up Firebase auth listener to log token
        setupFirebaseAuthListener()
        
        // Get current FCM token if user is already logged in
        if (FirebaseAuth.getInstance().currentUser != null) {
            logFirebaseMessagingToken()
        }
    }
    
    /**
     * Initialize app with stored language setting
     */
    private fun initializeAppLanguage() {
        try {
            // Use runBlocking here because we need the language before continuing
            val locale = runBlocking { 
                languageManager.getAppLanguage().first() 
            }
            
            // Apply locale to both JVM and Android resources
            applyLocaleToApplication(locale)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Sets up a Firebase authentication state listener to log FCM token
     * when user signs in or registers
     */
    private fun setupFirebaseAuthListener() {
        FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in - log FCM token
                logFirebaseMessagingToken()
            }
        }
    }
    
    /**
     * Retrieves and logs the Firebase Cloud Messaging token
     */
    private fun logFirebaseMessagingToken() {
        applicationScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d(TAG, "FCM Token: $token")
                
                // You can also store the token in your backend if needed
                // sendTokenToServer(token)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get FCM token", e)
            }
        }
    }
    
    override fun attachBaseContext(base: Context) {
        // Start with system default locale for initial attachment
        super.attachBaseContext(base)
        
        // We'll update with proper locale in onCreate
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        
        // When system configuration changes, make sure our app's selected language is prioritized
        applicationScope.launch {
            val storedLocale = languageManager.getAppLanguage().first()
            applyLocaleToApplication(storedLocale)
        }
    }
    
    private fun observeLanguageChanges() {
        applicationScope.launch {
            languageManager.getAppLanguage().collectLatest { locale ->
                applyLocaleToApplication(locale)
            }
        }
    }
    
    private fun applyLocaleToApplication(locale: Locale) {
        // 1. Set JVM default locale (affects string formatting)
        Locale.setDefault(locale)
        
        // 2. Apply at the application level through AppCompatDelegate
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)
        
        // 3. Update resources configuration
        val config = resources.configuration.apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
        
        // This ensures base context is always updated
        val ctx = LocaleHelper.setLocale(this, locale)
    }
    
    /**
     * Activity lifecycle callbacks to handle configuration changes
     */
    private class LocaleActivityLifecycleCallbacks(private val app: CineVibeApp) : 
            android.app.Application.ActivityLifecycleCallbacks {
        
        override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: android.os.Bundle?) {
            // Apply current locale to each new activity
            app.applicationScope.launch {
                val locale = app.languageManager.getAppLanguage().first()
                val context = LocaleHelper.setLocale(activity, locale)
                
                // Update resources with locale
                val config = activity.resources.configuration.apply {
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
                
                @Suppress("DEPRECATION")
                activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
            }
        }
        
        // Implement other methods with empty bodies
        override fun onActivityStarted(activity: android.app.Activity) {}
        override fun onActivityResumed(activity: android.app.Activity) {}
        override fun onActivityPaused(activity: android.app.Activity) {}
        override fun onActivityStopped(activity: android.app.Activity) {}
        override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: android.os.Bundle) {}
        override fun onActivityDestroyed(activity: android.app.Activity) {}
    }
}