package com.ttt.cinevibe.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth

/**
 * Utility class to help with Firebase initialization and checking for Google Play Services
 */
object FirebaseInitializer {
    private const val TAG = "FirebaseInitializer"
    private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000

    /**
     * Initializes Firebase and ensures Google Play Services are available
     * @return true if initialization was successful
     */
    fun initializeFirebase(context: Context): Boolean {
        try {
            // Initialize Firebase with extra error handling
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    Log.d(TAG, "No Firebase app found, initializing...")
                    FirebaseApp.initializeApp(context)
                } else {
                    Log.d(TAG, "Firebase app already initialized")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Firebase app", e)
                
                // Try to handle specific initialization problems
                if (e.message?.contains("API key") == true) {
                    Log.e(TAG, "Firebase API key issue - check your google-services.json file")
                } else if (e.message?.contains("security") == true || 
                           e.message?.contains("provider") == true) {
                    Log.e(TAG, "Security provider issue - attempting to update via Play Services")
                    updateSecurityProvider(context)
                }
                
                return false
            }
            
            // Check for Google Play Services
            if (!checkPlayServices(context)) {
                Log.e(TAG, "Google Play Services not available")
                return false
            }
            
            // Verify Firebase Auth is also initialized
            try {
                FirebaseAuth.getInstance()
                Log.d(TAG, "Firebase Auth initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Firebase Auth, even though main Firebase is initialized", e)
                return false
            }
            
            Log.d(TAG, "Firebase fully initialized successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase", e)
            return false
        }
    }

    /**
     * Update the Android Security Provider
     */
    private fun updateSecurityProvider(context: Context): Boolean {
        return try {
            ProviderInstaller.installIfNeeded(context)
            Log.d(TAG, "Security provider updated successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update security provider", e)
            false
        }
    }

    /**
     * Checks if Google Play Services are available and up to date
     */
    fun checkPlayServices(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Google Play Services availability result code: $resultCode")
            
            if (googleApiAvailability.isUserResolvableError(resultCode) && context is Activity) {
                Log.d(TAG, "Google Play Services error is resolvable by user")
                googleApiAvailability.getErrorDialog(
                    context, 
                    resultCode,
                    PLAY_SERVICES_RESOLUTION_REQUEST
                )?.show()
            } else {
                Log.e(TAG, "Google Play Services error is not resolvable: $resultCode")
            }
            return false
        }
        
        Log.d(TAG, "Google Play Services available and up to date")
        return true
    }
    
    /**
     * Handles the resolution result from checking Google Play Services
     */
    fun handlePlayServicesResolutionResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == PLAY_SERVICES_RESOLUTION_REQUEST) {
            return resultCode == Activity.RESULT_OK
        }
        return false
    }
    
    /**
     * Checks if Firebase Authentication is properly initialized
     */
    fun isFirebaseAuthInitialized(): Boolean {
        return try {
            FirebaseAuth.getInstance()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Auth not initialized", e)
            false
        }
    }
} 