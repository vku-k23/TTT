package com.ttt.cinevibe.presentation.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {
    
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
            
            try {
                movieRepository.searchMovies(query).collectLatest { results ->
                    _searchState.value = SearchState.Success(results)
                }
            } catch (e: Exception) {
                _searchState.value = SearchState.Error("Failed to search: ${e.message}")
            }
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