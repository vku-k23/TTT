package com.ttt.cinevibe.data.remote

import android.content.Context
import android.net.Uri
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class CloudinaryService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    // Cloudinary upload URL - Replace với cloud name của bạn
    private val cloudinaryUploadUrl = "https://api.cloudinary.com/v1_1/dm5bdbi6w/image/upload"
    private val uploadPreset = "ml_default" // Replace với upload preset của bạn

    suspend fun uploadImage(context: Context, imageUri: Uri): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Convert Uri to File
                val file = convertUriToFile(context, imageUri)
                    ?: return@withContext Resource.Error("Failed to process image")

                // Create multipart request
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file", file.name,
                        file.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                    .addFormDataPart("upload_preset", uploadPreset)
                    .build()

                val request = Request.Builder()
                    .url(cloudinaryUploadUrl)
                    .post(requestBody)
                    .build()

                // Execute request
                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val jsonObject = JSONObject(responseBody)
                    val secureUrl = jsonObject.getString("secure_url")
                    return@withContext Resource.Success(secureUrl)
                } else {
                    return@withContext Resource.Error("Upload failed: ${response.message}")
                }
            } catch (e: Exception) {
                return@withContext Resource.Error("Upload failed: ${e.message}")
            }
        }
    }

    private fun convertUriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }
}