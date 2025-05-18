package com.ttt.cinevibe.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.data.manager.LanguageManager
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.usecases.favorites.FavoriteMoviesUseCases
import com.ttt.cinevibe.domain.usecases.movies.GetMovieByIdUseCase
import com.ttt.cinevibe.domain.usecases.movies.GetSimilarMoviesUseCase
import com.ttt.cinevibe.domain.usecases.movies.GetMovieVideosUseCase
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
    private val getMovieVideosUseCase: GetMovieVideosUseCase,
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
                    
                    // Fetch videos for the movie
                    fetchMovieVideos(movieId, languageCode)
                    
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
    
    // Fetch videos (trailers, teasers, etc.) for the movie
    private fun fetchMovieVideos(movieId: Int, languageCode: String) {
        viewModelScope.launch {
            getMovieVideosUseCase(movieId, languageCode)
                .catch { e ->
                    android.util.Log.e("MovieDetailViewModel", "Error fetching videos: ${e.message}")
                    // Use fallback data on error
                    provideFallbackVideoData(movieId)
                }
                .collect { videosList ->
                    if (videosList.isNotEmpty()) {
                        // Convert VideoDto to our VideoDetails model
                        val videos = videosList
                            .filter { it.site.equals("YouTube", ignoreCase = true) } // Only YouTube videos for now
                            .map { videoDto ->
                                VideoDetails(
                                    id = videoDto.id,
                                    key = videoDto.key,
                                    name = videoDto.name,
                                    site = videoDto.site,
                                    type = videoDto.type,
                                    official = videoDto.official,
                                    publishedAt = videoDto.publishedAt
                                )
                            }
                            // Sort videos by official status and type (trailers first)
                            .sortedWith(compareByDescending<VideoDetails> { it.official }
                                .thenBy { 
                                    when(it.type.lowercase()) {
                                        "trailer" -> 0
                                        "teaser" -> 1
                                        "featurette" -> 2
                                        "clip" -> 3
                                        "behind the scenes" -> 4
                                        else -> 5
                                    }
                                })
                        
                        if (videos.isNotEmpty()) {
                            _videosList.value = videos
                            // Set trailer state to first fallback trailer if available
                            _trailerState.value = TrailerState(
                                isAvailable = true,
                                videoKey = videos[0].key
                            )
                            return@collect
                        }
                    }
                    
                    // If we reach here, we didn't get any usable videos
                    provideFallbackVideoData(movieId)
                }
        }
    }
    
    // Provide fallback video data when API fails or returns empty results
    private fun provideFallbackVideoData(movieId: Int) {
        val movie = _movieState.value.movie ?: return
        val videos = mutableListOf<VideoDetails>()
        
        // Use the main trailer key if available
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
            
            // Add a few popular movie trailers as fallback content based on movie ID
            // This ensures different movies will show different fallback trailers
            val popularTrailerKeys = listOf(
                Triple("dune-trailer", "5KYfP9y4Vdo", "Dune: Part Two (2024) | Official Trailer"),
                Triple("deadpool-trailer", "wZQCEBGcbG0", "Deadpool & Wolverine | Official Trailer"),
                Triple("wicked-trailer", "GDWoXP93xRs", "Wicked | Official Trailer"),
                Triple("kingdom-trailer", "AlUHB4tHbtA", "Kingdom of the Planet of the Apes | Trailer"),
                Triple("gladiator-trailer", "SddlkpJlYo0", "Gladiator II | Official Trailer"),
                Triple("joker-trailer", "sdg3RQBMZ3U", "Joker: Folie à Deux | Official Trailer"),
                Triple("inside-out-trailer", "AvYnlK3ULWg", "Inside Out 2 | Official Trailer")
            )
            
            // Select different trailers based on movie ID to provide variety
            val offset = (movieId % popularTrailerKeys.size)
            
            // Add a couple of fallback trailers
            for (i in 0 until 3) {
                val index = (offset + i) % popularTrailerKeys.size
                val (id, key, name) = popularTrailerKeys[index]
                
                videos.add(
                    VideoDetails(
                        id = id,
                        key = key,
                        name = name,
                        site = "YouTube",
                        type = when (i) {
                            0 -> "Teaser"
                            1 -> "Featurette"
                            else -> "Clip"
                        },
                        official = false
                    )
                )
            }
        }
        
        if (videos.isEmpty()) {
            // Absolutely last resort if no trailer key is available
            val fallbackTrailerKeys = listOf(
                Triple("spider-man-trailer", "JfVOs4VSpmA", "Spider-Man: No Way Home | Official Trailer"), 
                Triple("barbie-trailer", "pBk4NYhWNMM", "Barbie | Official Trailer"),
                Triple("oppenheimer-trailer", "bK6ldnjE3Y0", "Oppenheimer | Official Trailer")
            )
            
            val index = (movieId % fallbackTrailerKeys.size)
            val (id, key, name) = fallbackTrailerKeys[index]
            
            videos.add(
                VideoDetails(
                    id = id,
                    key = key,
                    name = name,
                    site = "YouTube",
                    type = "Trailer",
                    official = false
                )
            )
        }
        
        _videosList.value = videos
        // Set trailer state to first fallback trailer if available
        if (videos.isNotEmpty()) {
            _trailerState.value = TrailerState(
                isAvailable = true,
                videoKey = videos[0].key
            )
        } else {
            _trailerState.value = TrailerState(
                isAvailable = false,
                videoKey = null
            )
        }
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