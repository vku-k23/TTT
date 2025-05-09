package com.ttt.cinevibe.data.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: String? = null
)

@Serializable
data class PageResponse<T>(
    val content: List<T>,
    val pageable: Pageable,
    val totalElements: Int,
    val totalPages: Int,
    val last: Boolean,
    val size: Int,
    val number: Int,
    val sort: Sort,
    val numberOfElements: Int,
    val first: Boolean,
    val empty: Boolean
)

@Serializable
data class Pageable(
    val pageNumber: Int,
    val pageSize: Int,
    val sort: Sort,
    val offset: Long,
    val paged: Boolean,
    val unpaged: Boolean
)

@Serializable
data class Sort(
    val empty: Boolean,
    val sorted: Boolean,
    val unsorted: Boolean
)