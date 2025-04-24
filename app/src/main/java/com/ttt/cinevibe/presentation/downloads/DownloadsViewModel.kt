package com.ttt.cinevibe.presentation.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.domain.model.DownloadedMovie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor() : ViewModel() {

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Empty)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    init {
        loadDownloads()
    }

    private fun loadDownloads() {
        viewModelScope.launch {
            // In a real app, this would fetch from a local database
            val downloads = getDummyDownloads()
            
            if (downloads.isEmpty()) {
                _downloadState.value = DownloadState.Empty
            } else {
                _downloadState.value = DownloadState.Content(downloads)
            }
        }
    }

    fun deleteDownload(movie: DownloadedMovie) {
        viewModelScope.launch {
            val currentState = _downloadState.value
            if (currentState is DownloadState.Content) {
                val updatedList = currentState.downloads.toMutableList().apply {
                    remove(movie)
                }
                
                if (updatedList.isEmpty()) {
                    _downloadState.value = DownloadState.Empty
                } else {
                    _downloadState.value = DownloadState.Content(updatedList)
                }
            }
        }
    }

    private fun getDummyDownloads(): List<DownloadedMovie> {
        return listOf(
            DownloadedMovie(
                id = 1,
                title = "The Crown",
                size = "1.2 GB",
                duration = "1h 45m",
                posterUrl = "https://via.placeholder.com/200x300?text=The+Crown"
            ),
            DownloadedMovie(
                id = 2,
                title = "Stranger Things",
                size = "850 MB",
                duration = "52m",
                posterUrl = "https://via.placeholder.com/200x300?text=Stranger+Things"
            ),
            DownloadedMovie(
                id = 3,
                title = "Bridgerton",
                size = "1.1 GB",
                duration = "1h 12m",
                posterUrl = "https://via.placeholder.com/200x300?text=Bridgerton"
            )
        )
    }
}

sealed class DownloadState {
    object Empty : DownloadState()
    data class Content(val downloads: List<DownloadedMovie>) : DownloadState()
}