package com.ttt.cinevibe.utils

import android.content.Context
import android.os.Build
import android.util.Log

/**
 * Utility class to handle Samsung Knox compatibility issues
 */
object KnoxCompatUtil {
    private const val TAG = "KnoxCompatUtil"
    
    /**
     * Check if the device is a Samsung device with Knox
     */
    fun isSamsungKnoxDevice(): Boolean {
        return Build.MANUFACTURER.equals("samsung", ignoreCase = true) &&
                isKnoxAvailable()
    }
    
    /**
     * Checks if Knox API is available on the device
     */
    private fun isKnoxAvailable(): Boolean {
        return try {
            // Try to load the Knox class to see if it's available
            Class.forName("com.samsung.android.knox.EnterpriseDeviceManager")
            true
        } catch (e: Exception) {
            // Knox not available
            Log.d(TAG, "Knox not available: ${e.message}")
            false
        }
    }
    
    /**
     * Safely run code that might interact with Knox
     * Returns true if the operation was successful, false if it encountered Knox-related issues
     */
    fun safeKnoxOperation(context: Context, operation: () -> Unit): Boolean {
        return try {
            // If there's a Samsung device with Knox, we need to be careful
            if (isSamsungKnoxDevice()) {
                Log.d(TAG, "Running operation on Samsung Knox device with caution")
                
                // Run the operation in a try-catch to handle Knox errors
                try {
                    operation()
                    true
                } catch (e: Exception) {
                    if (e.stackTrace.any { it.className.contains("knox", ignoreCase = true) }) {
                        Log.e(TAG, "Knox-related error: ${e.message}", e)
                        false
                    } else {
                        // Re-throw non-Knox errors
                        throw e
                    }
                }
            } else {
                // Non-Samsung device, just run the operation
                operation()
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in safeKnoxOperation: ${e.message}", e)
            false
        }
    }
} 