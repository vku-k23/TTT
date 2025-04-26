package com.ttt.cinevibe.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.usecases.movies.GetMovieByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val getMovieByIdUseCase: GetMovieByIdUseCase
) : ViewModel() {

    private val _movieState = MutableStateFlow(MovieDetailState(isLoading = false))
    val movieState: StateFlow<MovieDetailState> = _movieState
    
    // State for trailer playback
    private val _trailerState = MutableStateFlow(TrailerState())
    val trailerState: StateFlow<TrailerState> = _trailerState
    
    // Track the current fetch job to cancel it if needed
    private var currentFetchJob: Job? = null
    
    // Track the last requested movie ID to prevent duplicate requests
    private var lastRequestedMovieId: Int? = null

    fun getMovieById(movieId: Int) {
        // If we're already loading this movie, don't start another request
        if (movieId == lastRequestedMovieId && 
            (currentFetchJob != null && currentFetchJob?.isActive == true)) {
            return
        }
        
        // Cancel any existing job before starting a new one
        currentFetchJob?.cancel()
        lastRequestedMovieId = movieId
        
        _movieState.value = MovieDetailState(isLoading = true)
        
        currentFetchJob = viewModelScope.launch {
            getMovieByIdUseCase(movieId)
                .catch { e ->
                    _movieState.value = MovieDetailState(
                        isLoading = false,
                        error = e.message ?: "Failed to load movie details"
                    )
                }
                .collect { movie ->
                    _movieState.value = MovieDetailState(
                        movie = movie,
                        isLoading = false,
                        error = null
                    )
                    
                    // Set trailer state if movie has a trailer
                    if (movie.trailerVideoKey != null) {
                        _trailerState.value = TrailerState(
                            isAvailable = true,
                            videoKey = movie.trailerVideoKey
                        )
                    } else {
                        _trailerState.value = TrailerState(
                            isAvailable = false,
                            videoKey = null
                        )
                    }
                }
        }
    }
    
    fun toggleTrailerPlayback() {
        _trailerState.value = _trailerState.value.copy(
            isPlaying = !_trailerState.value.isPlaying
        )
    }
    
    fun playTrailerInPlace() {
        _trailerState.value = _trailerState.value.copy(
            isPlayingInPlace = true,
            isPlaying = true
        )
    }
    
    fun stopTrailerInPlace() {
        _trailerState.value = _trailerState.value.copy(
            isPlayingInPlace = false,
            isPlaying = false
        )
    }
    
    fun showTrailer() {
        _trailerState.value = _trailerState.value.copy(
            isVisible = true,
            isPlaying = true
        )
    }
    
    fun hideTrailer() {
        _trailerState.value = _trailerState.value.copy(
            isVisible = false,
            isPlaying = false
        )
    }
}

// State for trailer playback
data class TrailerState(
    val isAvailable: Boolean = false,
    val videoKey: String? = null,
    val isPlaying: Boolean = false,
    val isVisible: Boolean = false,
    val isPlayingInPlace: Boolean = false
)