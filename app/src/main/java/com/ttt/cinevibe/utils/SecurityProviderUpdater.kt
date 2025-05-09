package com.ttt.cinevibe.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import com.google.android.gms.security.ProviderInstaller.ProviderInstallListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Utility class to update Android's security provider to protect against SSL exploits
 */
object SecurityProviderUpdater {
    private const val TAG = "SecurityProviderUpdater"

    /**
     * Updates Android's security provider to protect against SSL exploits.
     * @return true if the update succeeded, false otherwise
     */
    fun updateSecurityProvider(context: Context): Boolean {
        return try {
            // Install latest security provider
            ProviderInstaller.installIfNeeded(context)
            Log.d(TAG, "Security Provider updated successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating Security Provider", e)
            // Provider is not available, we can try to recover
            handleProviderInstallError(context, e)
        }
    }

    /**
     * Updates Android's security provider asynchronously.
     * @param context The application context
     * @param callback Callback to be invoked when the update is complete
     */
    fun updateSecurityProviderAsync(context: Context, callback: (Boolean) -> Unit) {
        try {
            ProviderInstaller.installIfNeededAsync(context, object : ProviderInstallListener {
                override fun onProviderInstalled() {
                    Log.d(TAG, "Security Provider installed successfully")
                    callback(true)
                }

                override fun onProviderInstallFailed(errorCode: Int, intent: android.content.Intent?) {
                    Log.e(TAG, "Provider install failed with code: $errorCode")
                    callback(false)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating async Security Provider update", e)
            callback(false)
        }
    }

    /**
     * Handle errors that occur during provider installation
     */
    private fun handleProviderInstallError(context: Context, error: Exception): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        
        // Check if Google Play Services is available
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(context)
        
        if (apiAvailability.isUserResolvableError(resultCode)) {
            // The error can be resolved by user, but not programmatically here
            Log.d(TAG, "Security Provider update error is user-resolvable")
            return false
        }
        
        Log.e(TAG, "Security Provider update error is not user-resolvable: $resultCode")
        return false
    }
    
    /**
     * Suspending function to update the security provider for use in coroutines
     */
    suspend fun updateSecurityProviderSuspending(context: Context): Boolean = 
        suspendCancellableCoroutine { continuation ->
            updateSecurityProviderAsync(context) { result ->
                continuation.resume(result)
            }
        }
} 