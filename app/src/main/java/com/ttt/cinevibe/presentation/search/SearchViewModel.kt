package com.ttt.cinevibe.presentation.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.data.manager.LanguageManager
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.usecase.movies.GetMovieByIdUseCase
import com.ttt.cinevibe.domain.usecase.movies.SearchMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchMoviesUseCase: SearchMoviesUseCase,
    private val getMovieByIdUseCase: GetMovieByIdUseCase,
    private val languageManager: LanguageManager
) : ViewModel() {
    
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Initial)
    val searchState: StateFlow<SearchState> = _searchState
    
    val searchQuery = mutableStateOf("")
    private var searchJob: Job? = null
    
    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        
        if (query.isEmpty()) {
            _searchState.value = SearchState.Initial
            return
        }
        
        // Auto search after typing with debounce
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Debounce for 500ms
            performSearch()
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
                // Get the current app language
                val userLocale = languageManager.getAppLanguage().first()
                // Create language code in format "en-US" from locale
                val languageCode = "${userLocale.language}-${userLocale.country.ifEmpty { userLocale.language.uppercase() }}"
                
                // Use the search use case to search movies from TheMovieDB API
                searchMoviesUseCase(query, languageCode)
                    .catch { e -> 
                        _searchState.value = SearchState.Error("Failed to search: ${e.message}")
                    }
                    .collect { movies ->
                        if (movies.isEmpty()) {
                            _searchState.value = SearchState.Success(emptyList())
                        } else {
                            _searchState.value = SearchState.Success(movies)
                        }
                    }
            } catch (e: Exception) {
                _searchState.value = SearchState.Error("An error occurred: ${e.message}")
            }
        }
    }
    
    fun getMovieDetails(movieId: Int, onSuccess: (Movie) -> Unit) {
        viewModelScope.launch {
            try {
                // Get the current app language
                val userLocale = languageManager.getAppLanguage().first()
                val languageCode = "${userLocale.language}-${userLocale.country.ifEmpty { userLocale.language.uppercase() }}"
                
                getMovieByIdUseCase(movieId, languageCode)
                    .catch { e -> 
                        // Handle error silently
                        android.util.Log.e("SearchViewModel", "Error getting movie details: ${e.message}")
                    }
                    .collect { movie ->
                        onSuccess(movie)
                    }
            } catch (e: Exception) {
                android.util.Log.e("SearchViewModel", "Exception in getMovieDetails: ${e.message}")
            }
        }
    }
    
    fun getPopularCategories(): List<String> {
        return listOf(
            "Action", "Comedy", "Drama", "Thriller", "Science Fiction", 
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