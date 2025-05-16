package com.ttt.cinevibe.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ttt.cinevibe.data.manager.LanguageManager
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.model.MovieReview
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.domain.repository.MovieReviewRepository
import com.ttt.cinevibe.domain.usecase.favorites.FavoriteMoviesUseCases
import com.ttt.cinevibe.domain.usecase.movies.GetMovieByIdUseCase
import com.ttt.cinevibe.domain.usecase.movies.GetSimilarMoviesUseCase
import com.ttt.cinevibe.domain.usecase.movies.GetMovieVideosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val getMovieByIdUseCase: GetMovieByIdUseCase,
    private val getSimilarMoviesUseCase: GetSimilarMoviesUseCase,
    private val getMovieVideosUseCase: GetMovieVideosUseCase,
    private val favoriteMoviesUseCases: FavoriteMoviesUseCases,
    private val languageManager: LanguageManager,
    private val movieReviewRepository: MovieReviewRepository
) : ViewModel() {

    private val _movieState = MutableStateFlow(MovieDetailState(isLoading = false))
    val movieState: StateFlow<MovieDetailState> = _movieState
    
    // State for trailer playback
    private val _trailerState = MutableStateFlow(TrailerState())
    val trailerState: StateFlow<TrailerState> = _trailerState
    
    // State for favorite status
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite
    
    // State for review operations
    private val _reviewOperationState = MutableStateFlow<ReviewOperationState>(ReviewOperationState.Idle)
    val reviewOperationState = _reviewOperationState.asStateFlow()
    
    // State for has user reviewed this movie
    private val _hasReviewedState = MutableStateFlow<HasReviewedState>(HasReviewedState.Loading)
    val hasReviewedState = _hasReviewedState.asStateFlow()
    
    // State for similar movies
    private val _similarMovies = MutableStateFlow<List<Movie>>(emptyList())
    val similarMovies: StateFlow<List<Movie>> = _similarMovies
    
    // State for multiple video trailers
    private val _videosList = MutableStateFlow<List<VideoDetails>>(emptyList())
    val videosList: StateFlow<List<VideoDetails>> = _videosList
    
    // State for user's review of this movie
    private val _userReview = MutableStateFlow<MovieReview?>(null)
    val userReview = _userReview.asStateFlow()
    
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
                    
                    // Check if the user has reviewed this movie
                    checkUserReviewStatus(movieId)
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
                            
                            // Set the primary trailer if not already set
                            if (_trailerState.value.videoKey == null) {
                                val mainTrailer = videos.find { it.type.equals("Trailer", ignoreCase = true) && it.official }
                                    ?: videos.firstOrNull()
                                
                                mainTrailer?.let {
                                    _trailerState.value = TrailerState(
                                        isAvailable = true,
                                        videoKey = it.key
                                    )
                                }
                            }
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
      private fun checkUserReviewStatus(movieId: Int) {
        viewModelScope.launch {
            movieReviewRepository.hasUserReviewedMovie(movieId.toLong())
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _hasReviewedState.value = if (result.data == true) {
                                HasReviewedState.Reviewed
                            } else {
                                HasReviewedState.NotReviewed
                            }
                        }
                        is Resource.Error -> {
                            _hasReviewedState.value = HasReviewedState.Error(result.message ?: "Unknown error")
                        }
                        is Resource.Loading -> {
                            _hasReviewedState.value = HasReviewedState.Loading
                        }
                    }
                }
        }
    }
      fun getMovieReviews(movieId: Int) {
        viewModelScope.launch {
            movieReviewRepository.getMovieReviews(movieId.toLong(), page = 1, size = 10)
                .catch { e ->
                    android.util.Log.e("MovieDetailViewModel", "Error fetching reviews: ${e.message}")
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val reviews = result.data ?: emptyList()
                            android.util.Log.d("MovieDetailViewModel", "Received ${reviews.size} reviews")
                        }
                        is Resource.Error -> {
                            android.util.Log.e("MovieDetailViewModel", "Error: ${result.message}")
                        }
                        is Resource.Loading -> {
                            android.util.Log.d("MovieDetailViewModel", "Loading reviews...")
                        }
                    }
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
    }    // Check if user has already reviewed this movie and get the review if they have
    fun checkIfUserReviewed(movieId: Long) {
        viewModelScope.launch {
            // Reset operation state
            _reviewOperationState.value = ReviewOperationState.Idle
            
            movieReviewRepository.hasUserReviewedMovie(movieId)
                .collectLatest { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _hasReviewedState.value = HasReviewedState.Loading
                        }
                        is Resource.Success -> {
                            val hasReviewed = result.data ?: false
                            _hasReviewedState.value = HasReviewedState.Success(hasReviewed)
                            
                            // If user has reviewed, get their review using the direct API endpoint
                            if (hasReviewed) {
                                fetchUserReview(movieId)
                            } else {
                                // Clear any previously loaded review
                                _userReview.value = null
                            }
                        }
                        is Resource.Error -> {
                            _hasReviewedState.value = HasReviewedState.Error(result.message ?: "Failed to check review status")
                        }
                    }
                }
        }
    }
    
    // Create a review for a movie
    fun createReview(movieId: Long, rating: Int, content: String) {
        viewModelScope.launch {
            _reviewOperationState.value = ReviewOperationState.Loading
            android.util.Log.d("MovieDetailViewModel", "Creating review: movieId=$movieId, rating=$rating")
            
            movieReviewRepository.createReview(
                tmdbMovieId = movieId, 
                rating = rating.toFloat(), 
                content = content,
                movieTitle = _movieState.value.movie?.title ?: "",
                containsSpoilers = false
            )
                .collectLatest { result ->
                    when (result) {
                        is Resource.Loading -> {
                            // Already set above
                            android.util.Log.d("MovieDetailViewModel", "Review creation loading...")
                        }
                        is Resource.Success -> {
                            android.util.Log.d("MovieDetailViewModel", "Review creation successful")
                            _reviewOperationState.value = ReviewOperationState.Success
                            // Update has reviewed state
                            _hasReviewedState.value = HasReviewedState.Success(true)
                            
                            // Update the userReview value with the created review
                            if (result.data != null) {
                                _userReview.value = result.data
                                android.util.Log.d("MovieDetailViewModel", "Review created successfully with ID: ${result.data.id}")
                            } else {
                                // If we don't have the review data, fetch it
                                android.util.Log.d("MovieDetailViewModel", "No review data returned, fetching review")
                                fetchUserReview(movieId)
                            }
                        }
                        is Resource.Error -> {
                            android.util.Log.e("MovieDetailViewModel", "Review creation failed: ${result.message}")
                            _reviewOperationState.value = ReviewOperationState.Error(result.message ?: "Failed to create review")
                            // If it's a duplicate review error, try to fetch the existing review
                            if (result.message?.contains("already reviewed", ignoreCase = true) == true) {
                                android.util.Log.d("MovieDetailViewModel", "This is a duplicate review, fetching existing review")
                                fetchUserReview(movieId)
                            }
                        }
                    }
                }
        }
    }
      // Update an existing review
    fun updateReview(reviewId: Long, rating: Int, content: String) {
        viewModelScope.launch {
            _reviewOperationState.value = ReviewOperationState.Loading
            android.util.Log.d("MovieDetailViewModel", "Updating review: reviewId=$reviewId, rating=$rating")
            
            // Get the current movie details for required fields
            val movieId = _movieState.value.movie?.id?.toLong()
            val movieTitle = _movieState.value.movie?.title
            
            movieReviewRepository.updateReview(
                reviewId = reviewId, 
                rating = rating.toFloat(), 
                content = content,
                containsSpoilers = false,
                tmdbMovieId = movieId,
                movieTitle = movieTitle
            )
                .collectLatest { result ->
                    when (result) {
                        is Resource.Loading -> {
                            // Already set above
                        }
                        is Resource.Success -> {
                            // Always set to success first to ensure the dialog closes
                            _reviewOperationState.value = ReviewOperationState.Success
                            android.util.Log.d("MovieDetailViewModel", "Review update successful: ${result.data?.id}")
                            
                            // Update the userReview value with the updated review
                            if (result.data != null) {
                                _userReview.value = result.data
                                android.util.Log.d("MovieDetailViewModel", "Review updated successfully: ID=${result.data.id}")
                            } else {
                                // If data is null but operation was successful, try to refresh the review data
                                // The operation is still considered successful even if this refresh fails
                                if (movieId != null && movieId > 0) {
                                    try {
                                        fetchUserReview(movieId)
                                    } catch (e: Exception) {
                                        android.util.Log.e("MovieDetailViewModel", "Error fetching updated review, but update was successful: ${e.message}")
                                        // The state remains Success
                                    }
                                } else {
                                    android.util.Log.w("MovieDetailViewModel", "Cannot fetch updated review - movie ID not available")
                                }
                            }
                        }
                        is Resource.Error -> {
                            _reviewOperationState.value = ReviewOperationState.Error(
                                result.message ?: "Failed to update review"
                            )
                            android.util.Log.e("MovieDetailViewModel", "Failed to update review: ${result.message}")
                        }
                    }
                }
        }
    }
    
    // Delete an existing review
    fun deleteReview(reviewId: Long) {
        viewModelScope.launch {
            _reviewOperationState.value = ReviewOperationState.Loading
            
            movieReviewRepository.deleteReview(reviewId)
                .collectLatest { result ->
                    when (result) {
                        is Resource.Loading -> {
                            // Already set above
                        }
                        is Resource.Success -> {
                            _reviewOperationState.value = ReviewOperationState.Success
                            // Clear the user review and update has reviewed state
                            _userReview.value = null
                            _hasReviewedState.value = HasReviewedState.Success(false)
                        }
                        is Resource.Error -> {
                            _reviewOperationState.value = ReviewOperationState.Error(
                                result.message ?: "Failed to delete review"
                            )
                        }
                    }
                }
        }
    }
    
    // Get the user's review for this movie
    fun getUserReviewForMovie(movieId: Long) {
        viewModelScope.launch {
            // First check if the user has reviewed the movie
            movieReviewRepository.hasUserReviewedMovie(movieId)
                .collectLatest { hasReviewedResult ->
                    when (hasReviewedResult) {
                        is Resource.Loading -> {
                            _hasReviewedState.value = HasReviewedState.Loading
                        }
                        is Resource.Success -> {
                            val hasReviewed = hasReviewedResult.data ?: false
                            _hasReviewedState.value = HasReviewedState.Success(hasReviewed)
                            
                            if (hasReviewed) {
                                // The user has reviewed, now fetch the movie reviews to find their review
                                movieReviewRepository.getMovieReviews(movieId, 0, 100)
                                    .collectLatest { reviewsResult ->
                                        when (reviewsResult) {
                                            is Resource.Loading -> {
                                                // Already handling loading state
                                            }
                                            is Resource.Success -> {                                                // Find the user's review (current user's uid matches the review's user)
                                                val reviews = reviewsResult.data ?: emptyList()
                                                
                                                // Get authenticated user ID from FirebaseAuth or SharedPrefs
                                                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                                                
                                                // Find the review created by current user
                                                val userReview = if (currentUserId != null) {
                                                    reviews.find { it.userProfile.uid == currentUserId }
                                                } else {
                                                    null
                                                }
                                                
                                                _userReview.value = userReview
                                            }
                                            is Resource.Error -> {
                                                _hasReviewedState.value = HasReviewedState.Error(
                                                    reviewsResult.message ?: "Failed to get user's review"
                                                )
                                            }
                                        }
                                    }
                            }
                        }
                        is Resource.Error -> {
                            _hasReviewedState.value = HasReviewedState.Error(
                                hasReviewedResult.message ?: "Failed to check review status"
                            )
                        }
                    }
                }
        }
    }
    
    // Fetch user's review for this movie using the new direct API endpoint
    fun fetchUserReview(movieId: Long) {
        viewModelScope.launch {
            movieReviewRepository.getUserReviewForMovie(movieId)
                .collectLatest { result ->
                    when (result) {
                        is Resource.Loading -> {
                            // Already handling loading state in the UI
                        }
                        is Resource.Success -> {
                            _userReview.value = result.data
                            // Also update the hasReviewedState if we got a valid review
                            if (result.data != null) {
                                _hasReviewedState.value = HasReviewedState.Success(true)
                            }
                        }
                        is Resource.Error -> {
                            android.util.Log.e("MovieDetailViewModel", "Error fetching user review: ${result.message}")
                            // Only set to null if we're sure it doesn't exist (404)
                            if (result.message?.contains("haven't reviewed", ignoreCase = true) == true || 
                                result.message?.contains("404", ignoreCase = true) == true) {
                                _userReview.value = null
                                _hasReviewedState.value = HasReviewedState.Success(false)
                            }
                            // Otherwise, keep previous state
                        }
                    }
                }
        }
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

// Using ReviewOperationState and HasReviewedState from ReviewState.kt