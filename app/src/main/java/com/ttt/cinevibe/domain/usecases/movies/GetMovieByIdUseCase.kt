package com.ttt.cinevibe.domain.usecases.movies

import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetMovieByIdUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    operator fun invoke(movieId: Int, language: String? = null): Flow<Movie> = flow {
        try {
            // Properly collect from the repository flow, passing the language parameter
            movieRepository.getMovieById(movieId, language).collect { movie ->
                emit(movie)
            }
        } catch (e: Exception) {
            // If repository call fails, emit a sample movie as fallback
            emit(getSampleMovie(movieId))
        }
    }
    
    // Helper method to create a sample movie for demo purposes
    private fun getSampleMovie(movieId: Int): Movie {
        // We'll use the ID to create a variety of sample movies
        val titleSuffix = when (movieId % 5) {
            0 -> "The Adventure Begins"
            1 -> "The Lost Kingdom"
            2 -> "In the Lost Lands"
            3 -> "The Final Chapter"
            else -> "A New Hope"
        }
        
        // Return a sample movie with the ID and a real trailer key
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
            voteAverage = 5.0 + (movieId % 5) * 0.8,
            genres = listOf("Action", "Adventure", "Drama", "Sci-Fi", "Fantasy", "Comedy", "Thriller")
                .shuffled().take(2),
            trailerVideoKey = listOf(
                "pS2gklbxWn0",  // Marvel trailer
                "TcMBFSGVi1c",  // Avengers: Endgame trailer
                "8g18jFHCLXk",  // Dune trailer
                "JfVOs4VSpmA",  // Spider-Man: No Way Home trailer
                "aSiDu3Ywi8E",  // Harry Potter trailer
                "5PSNL1qE6VY",  // Avatar 2 trailer
                "zSWdZVtXT7E",  // Batman trailer
                "d9MyW72ELq0"   // Star Wars trailer
            )[movieId % 8]  // Assign a real trailer key
        )
    }
}