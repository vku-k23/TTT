package com.ttt.cinevibe.domain.usecase.movies

import com.ttt.cinevibe.data.remote.models.VideoDto
import com.ttt.cinevibe.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetMovieVideosUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    operator fun invoke(movieId: Int, language: String? = null): Flow<List<VideoDto>> = flow {
        try {
            movieRepository.getMovieVideos(movieId, language).collect { videos ->
                emit(videos)
            }
        } catch (e: Exception) {
            android.util.Log.e("GetMovieVideosUseCase", "Error getting videos: ${e.message}")
            emit(emptyList())
        }
    }
}