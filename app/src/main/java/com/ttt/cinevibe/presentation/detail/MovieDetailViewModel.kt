package com.ttt.cinevibe.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.domain.usecases.movies.GetMovieByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val getMovieByIdUseCase: GetMovieByIdUseCase
) : ViewModel() {

    private val _movieState = MutableStateFlow(MovieDetailState(isLoading = false))
    val movieState: StateFlow<MovieDetailState> = _movieState

    fun getMovieById(movieId: Int) {
        _movieState.value = MovieDetailState(isLoading = true)
        
        viewModelScope.launch {
            getMovieByIdUseCase(movieId)
                .catch { e ->
                    _movieState.value = MovieDetailState(
                        isLoading = false,
                        error = e.message ?: "Failed to load movie details"
                    )
                }
                .collect { movie ->
                    _movieState.value = MovieDetailState(
                        movie = movie,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
}