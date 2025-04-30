package com.ttt.cinevibe.data.repository

import android.util.Log
import com.ttt.cinevibe.data.remote.api.MovieApiService
import com.ttt.cinevibe.data.remote.models.VideoDto
import com.ttt.cinevibe.domain.model.TvSeries
import com.ttt.cinevibe.domain.repository.TvSeriesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TvSeriesRepositoryImpl @Inject constructor(
    private val apiService: MovieApiService
) : TvSeriesRepository {

    override suspend fun getPopularTvSeries(language: String?): Flow<List<TvSeries>> = flow {
        try {
            val response = apiService.getPopularTvSeries(language = language)
            emit(response.results.map { it.toDomainModel() })
        } catch (e: Exception) {
            Log.e("TvSeriesRepository", "Error fetching popular TV series", e)
            emit(emptyList())
        }
    }

    override suspend fun getTopRatedTvSeries(language: String?): Flow<List<TvSeries>> = flow {
        try {
            val response = apiService.getTopRatedTvSeries(language = language)
            emit(response.results.map { it.toDomainModel() })
        } catch (e: Exception) {
            Log.e("TvSeriesRepository", "Error fetching top rated TV series", e)
            emit(emptyList())
        }
    }

    override suspend fun getOnTheAirTvSeries(language: String?): Flow<List<TvSeries>> = flow {
        try {
            val response = apiService.getOnTheAirTvSeries(language = language)
            emit(response.results.map { it.toDomainModel() })
        } catch (e: Exception) {
            Log.e("TvSeriesRepository", "Error fetching on the air TV series", e)
            emit(emptyList())
        }
    }

    override suspend fun getTvSeriesDetails(seriesId: Int, language: String?): Flow<TvSeries> = flow {
        try {
            val response = apiService.getTvSeriesDetails(seriesId, language)
            emit(response.results.first().toDomainModel())
        } catch (e: Exception) {
            Log.e("TvSeriesRepository", "Error fetching TV series details", e)
            throw e
        }
    }

    override suspend fun getTvSeriesVideos(seriesId: Int, language: String?): Flow<List<VideoDto>> = flow {
        try {
            val response = apiService.getTvSeriesVideos(seriesId, language)
            emit(response.results)
        } catch (e: Exception) {
            Log.e("TvSeriesRepository", "Error fetching TV series videos", e)
            emit(emptyList())
        }
    }

    override suspend fun searchTvSeries(query: String, language: String?): Flow<List<TvSeries>> = flow {
        try {
            val response = apiService.searchTvSeries(query, language = language)
            emit(response.results.map { it.toDomainModel() })
        } catch (e: Exception) {
            Log.e("TvSeriesRepository", "Error searching TV series", e)
            emit(emptyList())
        }
    }
} 