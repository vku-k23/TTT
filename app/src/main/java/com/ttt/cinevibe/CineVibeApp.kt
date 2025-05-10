package com.ttt.cinevibe

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.ttt.cinevibe.data.manager.LanguageManager
import com.ttt.cinevibe.utils.FirebaseInitializer
import com.ttt.cinevibe.utils.LocaleHelper
import com.ttt.cinevibe.utils.SecurityProviderUpdater
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date
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

    // Time offset between device and server time in milliseconds
    var timeOffsetMillis: Long = 0L
        private set

    override fun onCreate() {
        super.onCreate()
        
        // Update Android Security Provider first
        SecurityProviderUpdater.updateSecurityProvider(this)
        
        // Initialize with stored language immediately
        initializeAppLanguage()
        
        // Set up language observer for changes during app lifecycle
        observeLanguageChanges()
        
        // Register for configuration changes
        registerActivityLifecycleCallbacks(LocaleActivityLifecycleCallbacks(this))
        
        // Initialize Firebase properly using our utility
        if (FirebaseInitializer.initializeFirebase(this)) {
            Log.d(TAG, "Firebase initialization successful")
            
            // Set up Firebase auth listener
            setupFirebaseAuthListener()
            
            // Get current FCM token if user is already logged in
            if (FirebaseAuth.getInstance().currentUser != null) {
                logFirebaseMessagingToken()
            }
        } else {
            Log.e(TAG, "Firebase initialization failed")
            // Try to update security provider asynchronously
            SecurityProviderUpdater.updateSecurityProviderAsync(this) { success ->
                if (success) {
                    Log.d(TAG, "Security provider updated, retrying Firebase initialization")
                    FirebaseInitializer.initializeFirebase(this)
                }
            }
        }
        
        // Check time synchronization
        checkDeviceTime()
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
        try {
            FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                if (user != null) {
                    // User is signed in - log FCM token
                    logFirebaseMessagingToken()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up Firebase auth listener", e)
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

    /**
     * Checks if the device time is properly synchronized with network time
     * This is important for Firebase token validation
     */
    private fun checkDeviceTime() {
        Thread {
            try {
                // Get current device time
                val deviceTime = System.currentTimeMillis()
                
                // Get network time from a reliable server
                val url = URL("https://www.google.com")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.connect()
                
                val serverTime = connection.getHeaderFieldDate("Date", deviceTime)
                connection.disconnect()
                
                // Calculate the difference
                val timeDifference = deviceTime - serverTime
                
                if (Math.abs(timeDifference) > 30000) { // 30 seconds
                    // Significant time difference detected
                    Log.e(TAG, "⚠️ DEVICE TIME MISMATCH DETECTED ⚠️")
                    Log.e(TAG, "Device time: ${Date(deviceTime)}")
                    Log.e(TAG, "Server time: ${Date(serverTime)}")
                    Log.e(TAG, "Difference: ${timeDifference/1000} seconds")
                    Log.e(TAG, "This will cause Firebase authentication problems!")
                    
                    // Store the offset for potential use in the app
                    timeOffsetMillis = timeDifference
                    
                    // Show a notification about the time issue if it's severe
                    if (Math.abs(timeDifference) > 5 * 60 * 1000) { // 5 minutes
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                this,
                                "Your device clock is not correctly set. This may cause authentication problems.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    Log.d(TAG, "Device time is properly synchronized. Difference: ${timeDifference/1000} seconds")
                    timeOffsetMillis = 0L
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking time synchronization", e)
            }
        }.start()
    }
}