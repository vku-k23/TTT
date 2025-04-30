package com.ttt.cinevibe.domain.usecases.tvseries

import com.ttt.cinevibe.domain.model.TvSeries
import com.ttt.cinevibe.domain.repository.TvSeriesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOnTheAirTvSeriesUseCase @Inject constructor(
    private val repository: TvSeriesRepository
) {
    suspend operator fun invoke(language: String? = null): Flow<List<TvSeries>> {
        return repository.getOnTheAirTvSeries(language)
    }
} 