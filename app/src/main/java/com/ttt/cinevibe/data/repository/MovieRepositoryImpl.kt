package com.ttt.cinevibe.data.repository

import com.ttt.cinevibe.data.remote.ApiConstants
import com.ttt.cinevibe.data.remote.api.MovieApiService
import com.ttt.cinevibe.data.remote.models.MovieDto
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val movieApiService: MovieApiService
) : MovieRepository {
    
    private var genresCache: Map<Int, String> = emptyMap()
    
    override suspend fun getPopularMovies(): Flow<List<Movie>> = flow {
        try {
            // Ensure we have genres loaded for mapping genre IDs to names
            if (genresCache.isEmpty()) {
                loadGenres()
            }
            
            val response = movieApiService.getPopularMovies(ApiConstants.API_KEY)
            emit(response.results.map { it.toDomainModel(genresCache) })
        } catch (e: Exception) {
            // In a real app, you would log the error or handle it more gracefully
            emit(emptyList())
        }
    }
    
    override suspend fun getTopRatedMovies(): Flow<List<Movie>> = flow {
        try {
            if (genresCache.isEmpty()) {
                loadGenres()
            }
            
            val response = movieApiService.getTopRatedMovies(ApiConstants.API_KEY)
            emit(response.results.map { it.toDomainModel(genresCache) })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    override suspend fun getTrendingMovies(): Flow<List<Movie>> = flow {
        try {
            if (genresCache.isEmpty()) {
                loadGenres()
            }
            
            val response = movieApiService.getTrendingMovies(ApiConstants.API_KEY)
            emit(response.results.map { it.toDomainModel(genresCache) })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    override suspend fun getUpcomingMovies(): Flow<List<Movie>> = flow {
        try {
            if (genresCache.isEmpty()) {
                loadGenres()
            }
            
            val response = movieApiService.getUpcomingMovies(ApiConstants.API_KEY)
            emit(response.results.map { it.toDomainModel(genresCache) })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    override suspend fun searchMovies(query: String): Flow<List<Movie>> = flow {
        try {
            if (genresCache.isEmpty()) {
                loadGenres()
            }
            
            val response = movieApiService.searchMovies(
                apiKey = ApiConstants.API_KEY,
                query = query
            )
            emit(response.results.map { it.toDomainModel(genresCache) })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    override suspend fun getGenres(): Flow<Map<Int, String>> = flow {
        try {
            if (genresCache.isEmpty()) {
                loadGenres()
            }
            emit(genresCache)
        } catch (e: Exception) {
            emit(emptyMap())
        }
    }
    
    private suspend fun loadGenres() {
        try {
            val genreResponse = movieApiService.getGenres(ApiConstants.API_KEY)
            genresCache = genreResponse.genres.associate { it.id to it.name }
        } catch (e: Exception) {
            // Handle error or retry logic
        }
    }
    
    // Extension function to convert MovieDto to domain Movie model
    private fun MovieDto.toDomainModel(genreMap: Map<Int, String>): Movie {
        val genreNames = this.genreIds.mapNotNull { genreMap[it] }
        return Movie(
            id = this.id,
            title = this.title,
            overview = this.overview,
            posterPath = this.posterPath?.let { "${ApiConstants.IMAGE_BASE_URL}${ApiConstants.POSTER_SIZE}$it" },
            backdropPath = this.backdropPath?.let { "${ApiConstants.IMAGE_BASE_URL}${ApiConstants.BACKDROP_SIZE}$it" },
            releaseDate = this.releaseDate,
            voteAverage = this.voteAverage,
            genres = genreNames
        )
    }
}