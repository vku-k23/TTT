package com.ttt.cinevibe.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.data.manager.LanguageManager
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.usecases.favorites.FavoriteMoviesUseCases
import com.ttt.cinevibe.domain.usecases.movies.GetMovieByIdUseCase
import com.ttt.cinevibe.domain.usecases.movies.GetSimilarMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val getMovieByIdUseCase: GetMovieByIdUseCase,
    private val getSimilarMoviesUseCase: GetSimilarMoviesUseCase,
    private val favoriteMoviesUseCases: FavoriteMoviesUseCases,
    private val languageManager: LanguageManager
) : ViewModel() {

    private val _movieState = MutableStateFlow(MovieDetailState(isLoading = false))
    val movieState: StateFlow<MovieDetailState> = _movieState
    
    // State for trailer playback
    private val _trailerState = MutableStateFlow(TrailerState())
    val trailerState: StateFlow<TrailerState> = _trailerState
    
    // State for favorite status
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite
    
    // State for similar movies
    private val _similarMovies = MutableStateFlow<List<Movie>>(emptyList())
    val similarMovies: StateFlow<List<Movie>> = _similarMovies
    
    // State for multiple video trailers
    private val _videosList = MutableStateFlow<List<VideoDetails>>(emptyList())
    val videosList: StateFlow<List<VideoDetails>> = _videosList
    
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
        
        // Check if the movie is in favorites
        checkFavoriteStatus(movieId)
        
        currentFetchJob = viewModelScope.launch {
            // Use the user's selected language from LanguageManager
            val userLocale = languageManager.getAppLanguage().first()
            // Create language code in format "en-US" from locale
            val languageCode = "${userLocale.language}-${userLocale.country.ifEmpty { userLocale.language.uppercase() }}"
            
            // Log the language being used
            android.util.Log.d("MovieDetailViewModel", "Fetching movie details using language: $languageCode")
            
            getMovieByIdUseCase(movieId, languageCode)
                .catch { e ->
                    android.util.Log.e("MovieDetailViewModel", "Error loading movie details: ${e.message}")
                    _movieState.value = MovieDetailState(
                        isLoading = false,
                        error = e.message ?: "Failed to load movie details"
                    )
                }
                .collect { movie ->
                    // Log the received movie details to check if overview is available
                    android.util.Log.d("MovieDetailViewModel", "Received movie: title=${movie.title}, has overview: ${movie.overview?.isNotEmpty() == true}, overview length: ${movie.overview?.length ?: 0}")
                    
                    _movieState.value = MovieDetailState(
                        movie = movie,
                        isLoading = false,
                        error = null
                    )
                    
                    // Generate sample videos for the movie (for demonstration)
                    generateSampleVideos(movie)
                    
                    // Set primary trailer state if movie has a trailer
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
                    
                    // Fetch similar movies after getting movie details
                    fetchSimilarMovies(movieId, languageCode)
                }
        }
    }
    
    // Generate sample video trailers for the movie
    private fun generateSampleVideos(movie: Movie) {
        val videos = mutableListOf<VideoDetails>()
        
        // Add the main trailer if available
        movie.trailerVideoKey?.let { key ->
            videos.add(
                VideoDetails(
                    id = "main-trailer",
                    key = key,
                    name = "Official Trailer",
                    site = "YouTube",
                    type = "Trailer",
                    official = true
                )
            )
            
            // Add some dummy additional trailers for demonstration
            val dummyVideos = listOf(
                VideoDetails(
                    id = "teaser-1",
                    key = "TcMBFSGVi1c",  // Avengers: Endgame trailer
                    name = "Teaser Trailer",
                    site = "YouTube", 
                    type = "Teaser",
                    official = true
                ),
                VideoDetails(
                    id = "featurette-1",
                    key = "8g18jFHCLXk", // Dune trailer
                    name = "Behind The Scenes",
                    site = "YouTube",
                    type = "Featurette",
                    official = true
                ),
                VideoDetails(
                    id = "clip-1",
                    key = "aSiDu3Ywi8E", // Harry Potter trailer
                    name = "Extended Clip",
                    site = "YouTube",
                    type = "Clip",
                    official = true
                )
            )
            videos.addAll(dummyVideos)
        }
        
        // If no main trailer, at least add some sample videos
        if (videos.isEmpty()) {
            videos.add(
                VideoDetails(
                    id = "sample-1",
                    key = "JfVOs4VSpmA", // Spider-Man trailer
                    name = "Sample Trailer",
                    site = "YouTube",
                    type = "Trailer",
                    official = false
                )
            )
        }
        
        _videosList.value = videos
    }
    
    private fun fetchSimilarMovies(movieId: Int, languageCode: String) {
        viewModelScope.launch {
            getSimilarMoviesUseCase(movieId, languageCode)
                .catch { e ->
                    android.util.Log.e("MovieDetailViewModel", "Error loading similar movies: ${e.message}")
                    _similarMovies.value = emptyList()
                }
                .collect { movies ->
                    android.util.Log.d("MovieDetailViewModel", "Received ${movies.size} similar movies")
                    _similarMovies.value = movies
                }
        }
    }
    
    private fun checkFavoriteStatus(movieId: Int) {
        viewModelScope.launch {
            favoriteMoviesUseCases.isMovieFavorite(movieId)
                .collect { isFav ->
                    _isFavorite.value = isFav
                }
        }
    }
    
    fun toggleFavoriteStatus() {
        val movie = _movieState.value.movie ?: return
        viewModelScope.launch {
            if (_isFavorite.value) {
                favoriteMoviesUseCases.removeMovieFromFavorites(movie.id)
            } else {
                favoriteMoviesUseCases.addMovieToFavorites(movie)
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

    // Play a specific video by its key
    fun playVideo(videoKey: String) {
        _trailerState.value = _trailerState.value.copy(
            isAvailable = true,
            videoKey = videoKey,
            isPlayingInPlace = true,
            isPlaying = true
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

// Data class to hold video information
data class VideoDetails(
    val id: String,
    val key: String,
    val name: String,
    val site: String, // YouTube, Vimeo, etc.
    val type: String, // Trailer, Teaser, Featurette, etc.
    val official: Boolean = true,
    val publishedAt: String = ""
)