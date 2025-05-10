package com.ttt.cinevibe.data.repository

import android.util.Log
import com.ttt.cinevibe.data.remote.api.UserConnectionApiService
import com.ttt.cinevibe.data.remote.models.PageResponse
import com.ttt.cinevibe.data.remote.models.UserConnectionRequest
import com.ttt.cinevibe.data.remote.models.UserConnectionResponse
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UserConnectionRepositoryImpl @Inject constructor(
    private val userConnectionApiService: UserConnectionApiService
) : UserConnectionRepository {

    override suspend fun getFollowing(page: Int, size: Int): Flow<Resource<PageResponse<UserConnectionResponse>>> = flow {
        emit(Resource.Loading())
        try {
            Log.d("UserConnectionRepo", "Fetching following users: page=$page, size=$size")
            val response = userConnectionApiService.getFollowing(page, size)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            Log.e("UserConnectionRepo", "HTTP error fetching following: ${e.message}", e)
            emit(Resource.Error("Network error: ${e.message ?: "Unknown HTTP error"}"))
        } catch (e: IOException) {
            Log.e("UserConnectionRepo", "IO error fetching following: ${e.message}", e)
            emit(Resource.Error("Connection error: ${e.message ?: "Check your internet connection"}"))
        } catch (e: Exception) {
            Log.e("UserConnectionRepo", "Error fetching following: ${e.message}", e)
            emit(Resource.Error("Error: ${e.message ?: "Unknown error"}"))
        }
    }

    override suspend fun getFollowers(page: Int, size: Int): Flow<Resource<PageResponse<UserConnectionResponse>>> = flow {
        emit(Resource.Loading())
        try {
            Log.d("UserConnectionRepo", "Fetching followers: page=$page, size=$size")
            val response = userConnectionApiService.getFollowers(page, size)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            Log.e("UserConnectionRepo", "HTTP error fetching followers: ${e.message}", e)
            emit(Resource.Error("Network error: ${e.message ?: "Unknown HTTP error"}"))
        } catch (e: IOException) {
            Log.e("UserConnectionRepo", "IO error fetching followers: ${e.message}", e)
            emit(Resource.Error("Connection error: ${e.message ?: "Check your internet connection"}"))
        } catch (e: Exception) {
            Log.e("UserConnectionRepo", "Error fetching followers: ${e.message}", e)
            emit(Resource.Error("Error: ${e.message ?: "Unknown error"}"))
        }
    }

    override suspend fun getPendingRequests(page: Int, size: Int): Flow<Resource<PageResponse<UserConnectionResponse>>> = flow {
        emit(Resource.Loading())
        try {
            Log.d("UserConnectionRepo", "Fetching pending requests: page=$page, size=$size")
            val response = userConnectionApiService.getPendingRequests(page, size)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            Log.e("UserConnectionRepo", "HTTP error fetching pending requests: ${e.message}", e)
            emit(Resource.Error("Network error: ${e.message ?: "Unknown HTTP error"}"))
        } catch (e: IOException) {
            Log.e("UserConnectionRepo", "IO error fetching pending requests: ${e.message}", e)
            emit(Resource.Error("Connection error: ${e.message ?: "Check your internet connection"}"))
        } catch (e: Exception) {
            Log.e("UserConnectionRepo", "Error fetching pending requests: ${e.message}", e)
            emit(Resource.Error("Error: ${e.message ?: "Unknown error"}"))
        }
    }

    override suspend fun followUser(targetUserUid: String): Flow<Resource<UserConnectionResponse>> = flow {
        emit(Resource.Loading())
        try {
            Log.d("UserConnectionRepo", "Following user: $targetUserUid")
            val request = UserConnectionRequest(targetUserUid = targetUserUid)
            val response = userConnectionApiService.followUser(request)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            Log.e("UserConnectionRepo", "HTTP error following user: ${e.message}", e)
            emit(Resource.Error("Network error: ${e.message ?: "Unknown HTTP error"}"))
        } catch (e: IOException) {
            Log.e("UserConnectionRepo", "IO error following user: ${e.message}", e)
            emit(Resource.Error("Connection error: ${e.message ?: "Check your internet connection"}"))
        } catch (e: Exception) {
            Log.e("UserConnectionRepo", "Error following user: ${e.message}", e)
            emit(Resource.Error("Error: ${e.message ?: "Unknown error"}"))
        }
    }

    override suspend fun acceptFollowRequest(connectionId: Long): Flow<Resource<UserConnectionResponse>> = flow {
        emit(Resource.Loading())
        try {
            Log.d("UserConnectionRepo", "Accepting follow request: $connectionId")
            val response = userConnectionApiService.acceptFollowRequest(connectionId)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            Log.e("UserConnectionRepo", "HTTP error accepting follow request: ${e.message}", e)
            emit(Resource.Error("Network error: ${e.message ?: "Unknown HTTP error"}"))
        } catch (e: IOException) {
            Log.e("UserConnectionRepo", "IO error accepting follow request: ${e.message}", e)
            emit(Resource.Error("Connection error: ${e.message ?: "Check your internet connection"}"))
        } catch (e: Exception) {
            Log.e("UserConnectionRepo", "Error accepting follow request: ${e.message}", e)
            emit(Resource.Error("Error: ${e.message ?: "Unknown error"}"))
        }
    }

    override suspend fun rejectFollowRequest(connectionId: Long): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            Log.d("UserConnectionRepo", "Rejecting follow request: $connectionId")
            userConnectionApiService.rejectFollowRequest(connectionId)
            emit(Resource.Success(true))
        } catch (e: HttpException) {
            Log.e("UserConnectionRepo", "HTTP error rejecting follow request: ${e.message}", e)
            emit(Resource.Error("Network error: ${e.message ?: "Unknown HTTP error"}"))
        } catch (e: IOException) {
            Log.e("UserConnectionRepo", "IO error rejecting follow request: ${e.message}", e)
            emit(Resource.Error("Connection error: ${e.message ?: "Check your internet connection"}"))
        } catch (e: Exception) {
            Log.e("UserConnectionRepo", "Error rejecting follow request: ${e.message}", e)
            emit(Resource.Error("Error: ${e.message ?: "Unknown error"}"))
        }
    }

    override suspend fun unfollowUser(targetUserUid: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            Log.d("UserConnectionRepo", "Unfollowing user: $targetUserUid")
            userConnectionApiService.unfollowUser(targetUserUid)
            emit(Resource.Success(true))
        } catch (e: HttpException) {
            Log.e("UserConnectionRepo", "HTTP error unfollowing user: ${e.message}", e)
            emit(Resource.Error("Network error: ${e.message ?: "Unknown HTTP error"}"))
        } catch (e: IOException) {
            Log.e("UserConnectionRepo", "IO error unfollowing user: ${e.message}", e)
            emit(Resource.Error("Connection error: ${e.message ?: "Check your internet connection"}"))
        } catch (e: Exception) {
            Log.e("UserConnectionRepo", "Error unfollowing user: ${e.message}", e)
            emit(Resource.Error("Error: ${e.message ?: "Unknown error"}"))
        }
    }

    override suspend fun removeFollower(followerUid: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            Log.d("UserConnectionRepo", "Removing follower: $followerUid")
            userConnectionApiService.removeFollower(followerUid)
            emit(Resource.Success(true))
        } catch (e: HttpException) {
            Log.e("UserConnectionRepo", "HTTP error removing follower: ${e.message}", e)
            emit(Resource.Error("Network error: ${e.message ?: "Unknown HTTP error"}"))
        } catch (e: IOException) {
            Log.e("UserConnectionRepo", "IO error removing follower: ${e.message}", e)
            emit(Resource.Error("Connection error: ${e.message ?: "Check your internet connection"}"))
        } catch (e: Exception) {
            Log.e("UserConnectionRepo", "Error removing follower: ${e.message}", e)
            emit(Resource.Error("Error: ${e.message ?: "Unknown error"}"))
        }
    }

    override suspend fun checkConnectionStatus(targetUserUid: String): Flow<Resource<Map<String, Any>>> = flow {
        emit(Resource.Loading())
        try {
            Log.d("UserConnectionRepo", "Checking connection status with: $targetUserUid")
            val response = userConnectionApiService.checkConnectionStatus(targetUserUid)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            Log.e("UserConnectionRepo", "HTTP error checking connection status: ${e.message}", e)
            emit(Resource.Error("Network error: ${e.message ?: "Unknown HTTP error"}"))
        } catch (e: IOException) {
            Log.e("UserConnectionRepo", "IO error checking connection status: ${e.message}", e)
            emit(Resource.Error("Connection error: ${e.message ?: "Check your internet connection"}"))
        } catch (e: Exception) {
            Log.e("UserConnectionRepo", "Error checking connection status: ${e.message}", e)
            emit(Resource.Error("Error: ${e.message ?: "Unknown error"}"))
        }
    }
}