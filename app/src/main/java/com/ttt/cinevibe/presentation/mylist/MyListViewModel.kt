package com.ttt.cinevibe.presentation.mylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.usecases.favorites.FavoriteMoviesUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyListViewModel @Inject constructor(
    private val favoriteMoviesUseCases: FavoriteMoviesUseCases
) : ViewModel() {

    private val _favoriteMoviesState = MutableStateFlow<MyListState>(MyListState.Loading)
    val favoriteMoviesState: StateFlow<MyListState> = _favoriteMoviesState

    init {
        loadFavoriteMovies()
    }

    fun loadFavoriteMovies() {
        viewModelScope.launch {
            _favoriteMoviesState.value = MyListState.Loading
            
            favoriteMoviesUseCases.getFavoriteMovies()
                .catch { e ->
                    _favoriteMoviesState.value = MyListState.Error(e.message ?: "Failed to load favorite movies")
                }
                .collect { movies ->
                    if (movies.isEmpty()) {
                        _favoriteMoviesState.value = MyListState.Empty
                    } else {
                        _favoriteMoviesState.value = MyListState.Success(movies)
                    }
                }
        }
    }

    fun removeFromFavorites(movieId: Int) {
        viewModelScope.launch {
            favoriteMoviesUseCases.removeMovieFromFavorites(movieId)
            // The favoriteMovies flow will automatically update with the new list
        }
    }
}

sealed class MyListState {
    object Loading : MyListState()
    object Empty : MyListState()
    data class Success(val movies: List<Movie>) : MyListState()
    data class Error(val message: String) : MyListState()
}