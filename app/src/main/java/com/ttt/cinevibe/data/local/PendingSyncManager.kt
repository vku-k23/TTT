package com.ttt.cinevibe.data.local

import kotlinx.serialization.Serializable

/**
 * Interface quản lý các tác vụ đồng bộ hóa đang chờ xử lý
 * khi không có kết nối hoặc xảy ra lỗi tạm thời
 */
interface PendingSyncManager {
    /**
     * Lưu thông tin đăng ký đang chờ được đồng bộ với backend
     */
    suspend fun savePendingRegistration(email: String, displayName: String, username: String, firebaseUid: String)
    
    /**
     * Xóa thông tin đăng ký đang chờ sau khi đã đồng bộ thành công
     */
    suspend fun clearPendingRegistration()
    
    /**
     * Kiểm tra xem có đăng ký nào đang chờ đồng bộ không
     */
    suspend fun hasPendingRegistration(): Boolean
    
    /**
     * Lấy dữ liệu đăng ký đang chờ để đồng bộ
     */
    suspend fun getPendingRegistrationData(): RegistrationData?
}

/**
 * Data class chứa thông tin đăng ký người dùng đang chờ đồng bộ
 */
@Serializable
data class RegistrationData(
    val email: String,
    val displayName: String,
    val username: String,
    val firebaseUid: String
)