package com.ttt.cinevibe.presentation.newhot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.usecases.movies.GetTrendingMoviesUseCase
import com.ttt.cinevibe.domain.usecases.movies.GetUpcomingMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewHotViewModel @Inject constructor(
    private val getUpcomingMoviesUseCase: GetUpcomingMoviesUseCase,
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase
) : ViewModel() {
    
    private val _comingSoonMovies = MutableStateFlow<List<Movie>>(emptyList())
    val comingSoonMovies: StateFlow<List<Movie>> = _comingSoonMovies
    
    private val _everyoneWatchingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val everyoneWatchingMovies: StateFlow<List<Movie>> = _everyoneWatchingMovies
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    init {
        loadData()
    }
    
    fun loadData() {
        fetchUpcomingMovies()
        fetchTrendingMovies()
    }
    
    fun getComingSoonMovies(): List<Movie> = _comingSoonMovies.value
    
    fun getEveryoneWatchingMovies(): List<Movie> = _everyoneWatchingMovies.value
    
    private fun fetchUpcomingMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            getUpcomingMoviesUseCase()
                .catch { e -> 
                    _errorMessage.value = e.message ?: "Failed to load upcoming movies"
                    _isLoading.value = false
                }
                .collect { movies ->
                    _comingSoonMovies.value = movies
                    _isLoading.value = false
                }
        }
    }
    
    private fun fetchTrendingMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            getTrendingMoviesUseCase()
                .catch { e -> 
                    _errorMessage.value = e.message ?: "Failed to load trending movies"
                    _isLoading.value = false
                }
                .collect { movies ->
                    _everyoneWatchingMovies.value = movies
                    _isLoading.value = false
                }
        }
    }
    
    fun retry() {
        _errorMessage.value = null
        loadData()
    }
}