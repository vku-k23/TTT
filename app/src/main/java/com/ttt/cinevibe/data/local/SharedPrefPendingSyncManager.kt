package com.ttt.cinevibe.data.local

import android.content.Context
import android.content.SharedPreferences
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

    private val prefs: SharedPreferences by lazy { 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Lưu thông tin đăng ký đang chờ được đồng bộ với backend
     */
    override suspend fun savePendingRegistration(email: String, displayName: String, username: String, firebaseUid: String) = withContext(Dispatchers.IO) {
        val registrationData = RegistrationData(email, displayName, username, firebaseUid)
        val json = Json.encodeToString(registrationData)
        prefs.edit().putString(KEY_PENDING_REGISTRATION, json).apply()
    }
    
    /**
     * Xóa thông tin đăng ký đang chờ sau khi đã đồng bộ thành công
     */
    override suspend fun clearPendingRegistration() = withContext(Dispatchers.IO) {
        prefs.edit().remove(KEY_PENDING_REGISTRATION).apply()
    }
    
    /**
     * Kiểm tra xem có đăng ký nào đang chờ đồng bộ không
     */
    override suspend fun hasPendingRegistration(): Boolean = withContext(Dispatchers.IO) {
        prefs.contains(KEY_PENDING_REGISTRATION)
    }
    
    /**
     * Lấy dữ liệu đăng ký đang chờ để đồng bộ
     */
    override suspend fun getPendingRegistrationData(): RegistrationData? = withContext(Dispatchers.IO) {
        val json = prefs.getString(KEY_PENDING_REGISTRATION, null)
        if (json != null) {
            try {
                Json.decodeFromString<RegistrationData>(json)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    companion object {
        private const val PREF_NAME = "cinevibe_sync_preferences"
        private const val KEY_PENDING_REGISTRATION = "pending_registration"
    }
}