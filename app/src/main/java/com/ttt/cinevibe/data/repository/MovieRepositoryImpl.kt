package com.ttt.cinevibe.data.repository

import android.util.Log
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
            
            val response = movieApiService.getPopularMovies()
            Log.d("MovieRepository", "Popular movies fetched: ${response.results.size}")
            emit(response.results.map { it.toDomainModel(genresCache) })
        } catch (e: Exception) {
            // In a real app, you would log the error or handle it more gracefully
            Log.e("MovieRepository", "Error fetching popular movies", e)
            emit(emptyList())
        }
    }
    
    override suspend fun getTopRatedMovies(): Flow<List<Movie>> = flow {
        try {
            if (genresCache.isEmpty()) {
                loadGenres()
            }
            
            val response = movieApiService.getTopRatedMovies()
            Log.d("MovieRepository", "Top rated movies fetched: ${response.results.size}")
            emit(response.results.map { it.toDomainModel(genresCache) })
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error fetching top rated movies", e)
            emit(emptyList())
        }
    }
    
    override suspend fun getTrendingMovies(): Flow<List<Movie>> = flow {
        try {
            if (genresCache.isEmpty()) {
                loadGenres()
            }
            
            val response = movieApiService.getTrendingMovies()
            Log.d("MovieRepository", "Trending movies fetched: ${response.results.size}")
            emit(response.results.map { it.toDomainModel(genresCache) })
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error fetching trending movies", e)
            emit(emptyList())
        }
    }
    
    override suspend fun getUpcomingMovies(): Flow<List<Movie>> = flow {
        try {
            if (genresCache.isEmpty()) {
                loadGenres()
            }
            
            val response = movieApiService.getUpcomingMovies()
            Log.d("MovieRepository", "Upcoming movies fetched: ${response.results.size}")
            emit(response.results.map { it.toDomainModel(genresCache) })
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error fetching upcoming movies", e)
            emit(emptyList())
        }
    }
    
    override suspend fun searchMovies(query: String): Flow<List<Movie>> = flow {
        try {
            if (genresCache.isEmpty()) {
                loadGenres()
            }
            
            val response = movieApiService.searchMovies(query = query)
            Log.d("MovieRepository", "Search movies fetched: ${response.results.size}")
            emit(response.results.map { it.toDomainModel(genresCache) })
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error searching movies", e)
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
    
    override suspend fun getMovieById(movieId: Int): Flow<Movie> = flow {
        try {
            if (genresCache.isEmpty()) {
                loadGenres()
            }
            
            val movieDetails = movieApiService.getMovieDetails(movieId)
            Log.d("MovieRepository", "Movie details fetched for ID: $movieId")
            
            // Convert to domain model - handle the genre objects from the detail endpoint
            val genreNames = movieDetails.genres?.map { it.name } ?: emptyList()
            
            val movie = Movie(
                id = movieDetails.id,
                title = movieDetails.title,
                overview = movieDetails.overview,
                posterPath = movieDetails.posterPath,
                backdropPath = movieDetails.backdropPath,
                releaseDate = movieDetails.releaseDate,
                voteAverage = movieDetails.voteAverage,
                genres = genreNames
            )
            
            emit(movie)
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error fetching movie details for ID: $movieId", e)
            // In a real app, you might want to emit a more specific error or handle this differently
            throw e
        }
    }
    
    private suspend fun loadGenres() {
        try {
            val genreResponse = movieApiService.getGenres()
            genresCache = genreResponse.genres.associate { it.id to it.name }
        } catch (e: Exception) {
            // Handle error or retry logic
            Log.e("MovieRepository", "Error loading genres", e)
        }
    }
    
    // Extension function to convert MovieDto to domain Movie model
    private fun MovieDto.toDomainModel(genreMap: Map<Int, String>): Movie {
        val genreNames = this.genreIds.mapNotNull { genreMap[it] }
        return Movie(
            id = this.id,
            title = this.title,
            overview = this.overview,
            posterPath = this.posterPath, // Store just the path, not the full URL
            backdropPath = this.backdropPath, // Store just the path, not the full URL
            releaseDate = this.releaseDate,
            voteAverage = this.voteAverage,
            genres = genreNames
        )
    }
}