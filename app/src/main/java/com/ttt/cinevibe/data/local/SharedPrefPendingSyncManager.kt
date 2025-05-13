package com.ttt.cinevibe.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Triển khai PendingSyncManager sử dụng SharedPreferences để lưu trữ dữ liệu đồng bộ đang chờ xử lý
 */
@Singleton
class SharedPrefPendingSyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PendingSyncManager {

    private val TAG = "PendingSyncManager"
    private val prefs: SharedPreferences by lazy { 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }    /**
     * Lưu thông tin đăng ký đang chờ được đồng bộ với backend
     */
    override suspend fun savePendingRegistration(email: String, displayName: String, username: String, firebaseUid: String): Unit = withContext(Dispatchers.IO) {
        try {
            val registrationData = RegistrationData(email, displayName, username, firebaseUid)
            val json = Json.encodeToString(registrationData)
            prefs.edit().putString(KEY_PENDING_REGISTRATION, json).apply()
            Log.d(TAG, "Saved pending registration for user: $firebaseUid, username: $username")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving pending registration: ${e.message}", e)
        }
    }
      /**
     * Xóa thông tin đăng ký đang chờ sau khi đã đồng bộ thành công
     */
    override suspend fun clearPendingRegistration(): Unit = withContext(Dispatchers.IO) {
        try {
            prefs.edit().remove(KEY_PENDING_REGISTRATION).apply()
            Log.d(TAG, "Cleared pending registration data")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing pending registration: ${e.message}", e)
        }
    }
      /**
     * Xóa mọi dữ liệu đồng bộ đang chờ xử lý
     */
    override suspend fun clearAllPendingData(): Unit = withContext(Dispatchers.IO) {
        try {
            // Clear all data in the shared preferences
            prefs.edit().clear().apply()
            Log.d(TAG, "Cleared all pending sync data")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all pending data: ${e.message}", e)
        }
    }
    
    /**
     * Kiểm tra xem có đăng ký nào đang chờ đồng bộ không
     */
    override suspend fun hasPendingRegistration(): Boolean = withContext(Dispatchers.IO) {
        return@withContext prefs.contains(KEY_PENDING_REGISTRATION)
    }
    
    /**
     * Lấy dữ liệu đăng ký đang chờ để đồng bộ
     */
    override suspend fun getPendingRegistration(): RegistrationData? = withContext(Dispatchers.IO) {
        val json = prefs.getString(KEY_PENDING_REGISTRATION, null)
        if (json != null) {
            try {
                val data = Json.decodeFromString<RegistrationData>(json)
                Log.d(TAG, "Retrieved pending registration for user: ${data.firebaseUid}")
                return@withContext data
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing pending registration data: ${e.message}", e)
                return@withContext null
            }
        } else {
            Log.d(TAG, "No pending registration data found")
            return@withContext null
        }
    }

    companion object {
        private const val PREF_NAME = "cinevibe_sync_preferences"
        private const val KEY_PENDING_REGISTRATION = "pending_registration"
    }
}