package com.ttt.cinevibe.domain.model

data class DownloadedMovie(
    val id: Int,
    val title: String,
    val posterUrl: String,
    val size: String,
    val duration: String
)