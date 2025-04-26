package com.ttt.cinevibe.domain.usecases.movies

import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetMovieByIdUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    operator fun invoke(movieId: Int): Flow<Movie> = flow {
        // For demonstration purposes, we'll simulate getting a movie by ID
        // In a real app, you would fetch this from the repository
        val movie = getSampleMovie(movieId)
        emit(movie)
    }
    
    // Helper method to create a sample movie for demo purposes
    // In a real app, this would come from the API via the repository
    private fun getSampleMovie(movieId: Int): Movie {
        // We'll use the ID to create a variety of sample movies
        val titleSuffix = when (movieId % 5) {
            0 -> "The Adventure Begins"
            1 -> "The Lost Kingdom"
            2 -> "In the Lost Lands"
            3 -> "The Final Chapter"
            else -> "A New Hope"
        }
        
        val voteAverage = 5.0 + (movieId % 5) * 0.8
        val genres = listOf(
            "Action", "Adventure", "Drama", "Sci-Fi", "Fantasy", "Comedy", "Thriller"
        )
        
        // Return a sample movie with the ID
        return Movie(
            id = movieId,
            title = "Movie $movieId: $titleSuffix",
            overview = "This epic story follows our heroes as they embark on a journey unlike any other. " +
                    "Facing insurmountable odds and powerful enemies, they must find the strength within " +
                    "themselves to overcome the challenges that lie ahead. A tale of courage, friendship, " +
                    "and sacrifice that will keep you on the edge of your seat.",
            posterPath = "/poster_path_$movieId.jpg",
            backdropPath = "/backdrop_path_$movieId.jpg",
            releaseDate = "2025-${(movieId % 12) + 1}-${(movieId % 28) + 1}",
            voteAverage = voteAverage,
            genres = genres.shuffled().take(2)
        )
    }
}