package com.ttt.cinevibe.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.usecases.movies.GetPopularMoviesUseCase
import com.ttt.cinevibe.domain.usecases.movies.GetTopRatedMoviesUseCase
import com.ttt.cinevibe.domain.usecases.movies.GetTrendingMoviesUseCase
import com.ttt.cinevibe.domain.usecases.movies.GetUpcomingMoviesUseCase
import com.ttt.cinevibe.domain.usecases.movies.SearchMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
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
    private val searchMoviesUseCase: SearchMoviesUseCase
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
    private var _featuredMovie: Movie? = null

    init {
        fetchAllMovieData()
        observeSearchQuery()
    }
    
    private fun fetchAllMovieData() {
        fetchPopularMovies()
        fetchTopRatedMovies()
        fetchTrendingMovies()
        fetchUpcomingMovies()
    }
    
    fun getFeaturedMovie(): Movie {
        return _featuredMovie ?: _allMovies.value.firstOrNull() ?: Movie(
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
    
    fun getPopularMovies(): List<Movie> = _popularMovies.value
    
    fun getTopRatedMovies(): List<Movie> = _topRatedMovies.value
    
    fun getTrendingMovies(): List<Movie> = _trendingMovies.value
    
    fun getUpcomingMovies(): List<Movie> = _upcomingMovies.value
    
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

    private fun fetchPopularMovies() {
        viewModelScope.launch {
            getPopularMoviesUseCase()
                .onStart { _uiState.value = HomeUiState.Loading }
                .catch { e -> _uiState.value = HomeUiState.Error(e.message ?: "Unknown error") }
                .collect { movies ->
                    if (movies.isEmpty()) {
                        _uiState.value = HomeUiState.Error("No popular movies found")
                    } else {
                        _popularMovies.value = movies
                        _allMovies.value = (_allMovies.value + movies).distinctBy { it.id }
                        // Update featured movie if needed
                        updateFeaturedMovie()
                        _uiState.value = HomeUiState.Success(movies)
                    }
                }
        }
    }
    
    private fun fetchTopRatedMovies() {
        viewModelScope.launch {
            getTopRatedMoviesUseCase()
                .catch { /* Handle error silently as this is a background load */ }
                .collect { movies ->
                    _topRatedMovies.value = movies
                    _allMovies.value = (_allMovies.value + movies).distinctBy { it.id }
                    // Update featured movie
                    updateFeaturedMovie()
                }
        }
    }
    
    private fun fetchTrendingMovies() {
        viewModelScope.launch {
            getTrendingMoviesUseCase()
                .catch { /* Handle error silently as this is a background load */ }
                .collect { movies ->
                    _trendingMovies.value = movies
                    _allMovies.value = (_allMovies.value + movies).distinctBy { it.id }
                    // Update featured movie
                    updateFeaturedMovie()
                }
        }
    }
    
    private fun fetchUpcomingMovies() {
        viewModelScope.launch {
            getUpcomingMoviesUseCase()
                .catch { /* Handle error silently as this is a background load */ }
                .collect { movies ->
                    _upcomingMovies.value = movies
                    _allMovies.value = (_allMovies.value + movies).distinctBy { it.id }
                    // Update featured movie
                    updateFeaturedMovie()
                }
        }
    }
    
    private fun updateFeaturedMovie() {
        // Select a movie with the highest vote average as the featured movie
        _featuredMovie = _allMovies.value.maxByOrNull { it.voteAverage }
    }
    
    private fun searchMovies(query: String) {
        viewModelScope.launch {
            searchMoviesUseCase(query)
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