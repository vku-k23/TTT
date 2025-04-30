package com.ttt.cinevibe.domain.repository

import com.ttt.cinevibe.domain.model.TvSeries
import com.ttt.cinevibe.data.remote.models.VideoDto
import kotlinx.coroutines.flow.Flow

interface TvSeriesRepository {
    suspend fun getPopularTvSeries(language: String? = null): Flow<List<TvSeries>>
    suspend fun getTopRatedTvSeries(language: String? = null): Flow<List<TvSeries>>
    suspend fun getOnTheAirTvSeries(language: String? = null): Flow<List<TvSeries>>
    suspend fun getTvSeriesDetails(seriesId: Int, language: String? = null): Flow<TvSeries>
    suspend fun getTvSeriesVideos(seriesId: Int, language: String? = null): Flow<List<VideoDto>>
    suspend fun searchTvSeries(query: String, language: String? = null): Flow<List<TvSeries>>
} 