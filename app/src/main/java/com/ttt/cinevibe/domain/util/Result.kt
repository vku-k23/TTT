package com.ttt.cinevibe.domain.util

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()

    companion object {
        fun <T> loading(): Result<T> = Loading
        fun <T> success(data: T): Result<T> = Success(data)
        fun <T> error(message: String, exception: Exception? = null): Result<T> = Error(message, exception)
    }
}