package com.ttt.cinevibe.presentation.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.domain.model.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {
    
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Initial)
    val searchState: StateFlow<SearchState> = _searchState
    
    val searchQuery = mutableStateOf("")
    
    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        
        if (query.isEmpty()) {
            _searchState.value = SearchState.Initial
        }
    }
    
    fun performSearch() {
        val query = searchQuery.value.trim()
        if (query.isEmpty()) {
            _searchState.value = SearchState.Initial
            return
        }
        
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            
            // Simulate network delay
            delay(800)
            
            try {
                // In a real app, this would call a use case to perform a repository search
                val results = searchMovies(query)
                _searchState.value = SearchState.Success(results)
            } catch (e: Exception) {
                _searchState.value = SearchState.Error("Failed to search: ${e.message}")
            }
        }
    }
    
    private fun searchMovies(query: String): List<Movie> {
        // Simulate search results - in real app this would come from API
        val allDummyMovies = createDummyMovies()
        
        return allDummyMovies.filter {
            it.title.contains(query, ignoreCase = true) || 
            it.overview.contains(query, ignoreCase = true)
        }
    }
    
    private fun createDummyMovies(): List<Movie> {
        return List(15) { index ->
            val genre = when (index % 5) {
                0 -> "Action"
                1 -> "Comedy"
                2 -> "Drama"
                3 -> "Thriller"
                else -> "Sci-Fi"
            }
            
            Movie(
                id = 300 + index,
                title = "$genre Movie ${index + 1}",
                overview = "This is a $genre movie about exciting adventures and compelling characters.",
                posterPath = null, // Would be actual paths in production
                releaseDate = "2024-${(index % 12) + 1}-${(index % 28) + 1}",
                voteAverage = 3.5 + (index % 15) / 10.0
            )
        }
    }
    
    fun getPopularCategories(): List<String> {
        return listOf(
            "Action", "Comedy", "Drama", "Thriller", "Sci-Fi", 
            "Animation", "Horror", "Documentary", "Family", "Romance"
        )
    }
}

sealed class SearchState {
    object Initial : SearchState()
    object Loading : SearchState()
    data class Success(val movies: List<Movie>) : SearchState()
    data class Error(val message: String) : SearchState()
}