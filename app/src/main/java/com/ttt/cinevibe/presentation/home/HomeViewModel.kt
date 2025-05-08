package com.ttt.cinevibe.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.data.manager.LanguageManager
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.usecase.movies.GetMovieByIdUseCase
import com.ttt.cinevibe.domain.usecase.movies.GetPopularMoviesUseCase
import com.ttt.cinevibe.domain.usecase.movies.GetTopRatedMoviesUseCase
import com.ttt.cinevibe.domain.usecase.movies.GetTrendingMoviesUseCase
import com.ttt.cinevibe.domain.usecase.movies.GetUpcomingMoviesUseCase
import com.ttt.cinevibe.domain.usecase.movies.SearchMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
    private val getTopRatedMoviesUseCase: GetTopRatedMoviesUseCase,
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val getUpcomingMoviesUseCase: GetUpcomingMoviesUseCase,
    private val searchMoviesUseCase: SearchMoviesUseCase,
    private val getMovieByIdUseCase: GetMovieByIdUseCase,
    private val languageManager: LanguageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    // Store movies for different categories
    private val _allMovies = MutableStateFlow<List<Movie>>(emptyList())
    private val _popularMovies = MutableStateFlow<List<Movie>>(emptyList())
    private val _topRatedMovies = MutableStateFlow<List<Movie>>(emptyList())
    private val _trendingMovies = MutableStateFlow<List<Movie>>(emptyList())
    private val _upcomingMovies = MutableStateFlow<List<Movie>>(emptyList())
    private val _recentlyWatchedMovies = MutableStateFlow<List<Movie>>(emptyList())
    
    // Featured carousel movies and current position
    private val _featuredMovies = MutableStateFlow<List<Movie>>(emptyList())
    val featuredMovies: StateFlow<List<Movie>> = _featuredMovies
    private val _currentFeaturedMovieIndex = MutableStateFlow(0)
    val currentFeaturedMovieIndex: StateFlow<Int> = _currentFeaturedMovieIndex
    
    // Selected movie for details dialog
    private val _selectedMovie = MutableStateFlow<Movie?>(null)
    val selectedMovie: StateFlow<Movie?> = _selectedMovie
    
    // State to determine if the movie details dialog is showing
    private val _isMovieDetailsVisible = MutableStateFlow(false)
    val isMovieDetailsVisible: StateFlow<Boolean> = _isMovieDetailsVisible

    init {
        fetchAllMovieData()
        observeSearchQuery()
        // Simulate recently watched movies with a subset of trending or popular movies
        generateMockRecentlyWatchedMovies()
        // Start auto carousel
        startCarouselAutoSlide()
    }
    
    // Helper method to get current user language in the format required by the API
    private suspend fun getCurrentLanguageCode(): String {
        val userLocale = languageManager.getAppLanguage().first()
        // Create language code in format "en-US" from locale
        return "${userLocale.language}-${userLocale.country.ifEmpty { userLocale.language.uppercase() }}"
    }
    
    private fun fetchAllMovieData() {
        viewModelScope.launch {
            val languageCode = getCurrentLanguageCode()
            android.util.Log.d("HomeViewModel", "Fetching all movie data in language: $languageCode")
            fetchPopularMovies(languageCode)
            fetchTopRatedMovies(languageCode)
            fetchTrendingMovies(languageCode)
            fetchUpcomingMovies(languageCode)
        }
    }
    
    fun getFeaturedMovie(): Movie {
        return _featuredMovies.value.getOrNull(_currentFeaturedMovieIndex.value)
            ?: _featuredMovies.value.firstOrNull()
            ?: _allMovies.value.firstOrNull()
            ?: createPlaceholderMovie()
    }
    
    private fun createPlaceholderMovie(): Movie {
        return Movie(
            id = 0,
            title = "Featured Movie",
            overview = "This is a placeholder for a featured movie.",
            posterPath = "/poster_featured.jpg",
            backdropPath = "/backdrop_featured.jpg",
            releaseDate = "2025-04-23",
            voteAverage = 4.5,
            genres = listOf("Drama", "Thriller")
        )
    }
    
    private fun startCarouselAutoSlide() {
        viewModelScope.launch {
            while (true) {
                delay(5000) // Change slide every 5 seconds
                val currentIndex = _currentFeaturedMovieIndex.value
                val featuredMoviesCount = _featuredMovies.value.size
                if (featuredMoviesCount > 1) {
                    _currentFeaturedMovieIndex.value = (currentIndex + 1) % featuredMoviesCount
                }
            }
        }
    }
    
    fun nextFeaturedMovie() {
        _currentFeaturedMovieIndex.value = (_currentFeaturedMovieIndex.value + 1) % _featuredMovies.value.size
    }
    
    fun previousFeaturedMovie() {
        _currentFeaturedMovieIndex.value = if (_currentFeaturedMovieIndex.value > 0) {
            _currentFeaturedMovieIndex.value - 1
        } else {
            _featuredMovies.value.size - 1
        }
    }
    
    // New method to directly update the featured movie index for the auto-slide feature
    fun updateFeaturedMovieIndex(index: Int) {
        if (index in 0 until _featuredMovies.value.size) {
            _currentFeaturedMovieIndex.value = index
        }
    }
    
    fun selectMovie(movie: Movie) {
        viewModelScope.launch {
            try {
                // First set the selected movie to show the loading state
                _selectedMovie.value = movie
                _isMovieDetailsVisible.value = true
                
                // Get the current app language
                val userLocale = languageManager.getAppLanguage().first()
                // Create language code in format "en-US" from locale
                val languageCode = "${userLocale.language}-${userLocale.country.ifEmpty { userLocale.language.uppercase() }}"
                
                android.util.Log.d("HomeViewModel", "Fetching movie details in language: $languageCode")
                
                // Fetch localized movie details
                getMovieByIdUseCase(movie.id, languageCode)
                    .catch { e ->
                        android.util.Log.e("HomeViewModel", "Error loading localized movie details: ${e.message}")
                        // If there's an error, we'll just use the original movie
                    }
                    .collect { localizedMovie ->
                        // Update with localized movie data
                        _selectedMovie.value = localizedMovie
                    }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error in selectMovie: ${e.message}")
                // If all else fails, fall back to using the original movie
                _selectedMovie.value = movie
            }
        }
    }
    
    fun closeMovieDetails() {
        _isMovieDetailsVisible.value = false
    }
    
    fun getPopularMovies(): List<Movie> = _popularMovies.value
    
    fun getTopRatedMovies(): List<Movie> = _topRatedMovies.value
    
    fun getTrendingMovies(): List<Movie> = _trendingMovies.value
    
    fun getUpcomingMovies(): List<Movie> = _upcomingMovies.value
    
    // Function to get recently watched movies for the "Continue Watching" section
    fun getRecentlyWatchedMovies(): List<Movie> = _recentlyWatchedMovies.value
    
    // For demo purposes, simulate recently watched movies with a subset of trending or popular movies
    private fun generateMockRecentlyWatchedMovies() {
        viewModelScope.launch {
            // Wait until we have some movies loaded
            if (_allMovies.value.isNotEmpty()) {
                // Take a subset of movies to display as "Continue Watching"
                val continueWatchingMovies = _allMovies.value.take(4)
                _recentlyWatchedMovies.value = continueWatchingMovies
            } else {
                // Create a list of dummy movies specifically for the Money Heist and The Witcher
                // as seen in the image
                val moneyHeist = Movie(
                    id = 71446,
                    title = "Money Heist",
                    overview = "To carry out the biggest heist in history, a mysterious man called The Professor recruits a band of eight robbers who have a single characteristic: none of them has anything to lose.",
                    posterPath = "/reEMJA1uzscCbkpeRJeTT2bjqUp.jpg",
                    backdropPath = "/gFZriCkpJYsApPZEF3jhxL4yLzG.jpg",
                    releaseDate = "2017-05-02",
                    voteAverage = 8.3,
                    genres = listOf("Crime", "Drama", "Action")
                )
                
                val theWitcher = Movie(
                    id = 71912,
                    title = "The Witcher",
                    overview = "Geralt of Rivia, a mutated monster-hunter for hire, journeys toward his destiny in a turbulent world where people often prove more wicked than beasts.",
                    posterPath = "/7vjaCdMw15FEbXyLQTVa04URsPm.jpg",
                    backdropPath = "/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg",
                    releaseDate = "2019-12-20",
                    voteAverage = 8.2,
                    genres = listOf("Sci-Fi & Fantasy", "Drama", "Action & Adventure")
                )
                
                _recentlyWatchedMovies.value = listOf(moneyHeist, theWitcher)
            }
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
    
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // 300ms debounce
                .collectLatest { query ->
                    if (query.isEmpty()) {
                        fetchPopularMovies()
                    } else {
                        searchMovies(query)
                    }
                }
        }
    }

    private fun fetchPopularMovies(languageCode: String? = null) {
        viewModelScope.launch {
            val language = languageCode ?: getCurrentLanguageCode()
            android.util.Log.d("HomeViewModel", "Fetching popular movies in language: $language")
            
            getPopularMoviesUseCase(language)
                .onStart { _uiState.value = HomeUiState.Loading }
                .catch { e -> _uiState.value = HomeUiState.Error(e.message ?: "Unknown error") }
                .collect { movies ->
                    if (movies.isEmpty()) {
                        _uiState.value = HomeUiState.Error("No popular movies found")
                    } else {
                        _popularMovies.value = movies
                        _allMovies.value = (_allMovies.value + movies).distinctBy { it.id }
                        // Update featured movies
                        updateFeaturedMovies()
                        _uiState.value = HomeUiState.Success(movies)
                        // Update recently watched movies if needed
                        if (_recentlyWatchedMovies.value.isEmpty()) {
                            generateMockRecentlyWatchedMovies()
                        }
                    }
                }
        }
    }
    
    private fun fetchTopRatedMovies(languageCode: String? = null) {
        viewModelScope.launch {
            val language = languageCode ?: getCurrentLanguageCode()
            android.util.Log.d("HomeViewModel", "Fetching top rated movies in language: $language")
            
            getTopRatedMoviesUseCase(language)
                .catch { /* Handle error silently as this is a background load */ }
                .collect { movies ->
                    _topRatedMovies.value = movies
                    _allMovies.value = (_allMovies.value + movies).distinctBy { it.id }
                    // Update featured movie
                    updateFeaturedMovies()
                }
        }
    }
    
    private fun fetchTrendingMovies(languageCode: String? = null) {
        viewModelScope.launch {
            val language = languageCode ?: getCurrentLanguageCode()
            android.util.Log.d("HomeViewModel", "Fetching trending movies in language: $language")
            
            getTrendingMoviesUseCase(language)
                .catch { /* Handle error silently as this is a background load */ }
                .collect { movies ->
                    _trendingMovies.value = movies
                    _allMovies.value = (_allMovies.value + movies).distinctBy { it.id }
                    // Update featured movie
                    updateFeaturedMovies()
                }
        }
    }
    
    private fun fetchUpcomingMovies(languageCode: String? = null) {
        viewModelScope.launch {
            val language = languageCode ?: getCurrentLanguageCode()
            android.util.Log.d("HomeViewModel", "Fetching upcoming movies in language: $language")
            
            getUpcomingMoviesUseCase(language)
                .catch { /* Handle error silently as this is a background load */ }
                .collect { movies ->
                    _upcomingMovies.value = movies
                    _allMovies.value = (_allMovies.value + movies).distinctBy { it.id }
                    // Update featured movie
                    updateFeaturedMovies()
                }
        }
    }
    
    private fun updateFeaturedMovies() {
        // Get top 5 movies for the featured carousel
        val featuredMovies = _allMovies.value
            .filter { it.backdropPath != null } // Only movies with backdrop images
            .sortedByDescending { it.voteAverage }
            .take(5)
        
        if (featuredMovies.isNotEmpty()) {
            _featuredMovies.value = featuredMovies
        }
    }
    
    private fun searchMovies(query: String) {
        viewModelScope.launch {
            val languageCode = getCurrentLanguageCode()
            android.util.Log.d("HomeViewModel", "Searching movies for '$query' in language: $languageCode")
            
            searchMoviesUseCase(query, languageCode)
                .onStart { _uiState.value = HomeUiState.Loading }
                .catch { e -> _uiState.value = HomeUiState.Error(e.message ?: "Unknown error") }
                .collect { movies ->
                    if (movies.isEmpty()) {
                        _uiState.value = HomeUiState.Error("No movies found for '$query'")
                    } else {
                        _uiState.value = HomeUiState.Success(movies)
                    }
                }
        }
    }
    
    fun retry() {
        if (_searchQuery.value.isNotEmpty()) {
            searchMovies(_searchQuery.value)
        } else {
            fetchAllMovieData()
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val movies: List<Movie>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}