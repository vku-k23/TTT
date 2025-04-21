package com.ttt.cinevibe.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.domain.usecases.movies.GetPopularMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase
) : ViewModel() {

    private val _popularMovies = MutableStateFlow<List<com.ttt.cinevibe.domain.model.Movie>>(emptyList())
    val popularMovies: StateFlow<List<com.ttt.cinevibe.domain.model.Movie>> = _popularMovies

    init {
        getPopularMovies()
    }

    private fun getPopularMovies() {
        viewModelScope.launch {
            getPopularMoviesUseCase().collect { movies ->
                _popularMovies.value = movies
            }
        }
    }
}