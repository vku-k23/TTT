package com.ttt.cinevibe.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import android.util.Log
import com.ttt.cinevibe.R
import com.ttt.cinevibe.data.remote.ApiConstants
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onMovieClick: (Movie) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val featuredMovie = viewModel.getFeaturedMovie()
    val popularMovies = viewModel.getPopularMovies()
    val topRatedMovies = viewModel.getTopRatedMovies()
    val trendingMovies = viewModel.getTrendingMovies()
    val upcomingMovies = viewModel.getUpcomingMovies()

    // For debugging - log movie counts
    Log.d("HomeScreen", "Popular movies: ${popularMovies.size}")
    Log.d("HomeScreen", "Top rated movies: ${topRatedMovies.size}")
    Log.d("HomeScreen", "Trending movies: ${trendingMovies.size}")
    Log.d("HomeScreen", "Upcoming movies: ${upcomingMovies.size}")
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        when (uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = NetflixRed
                )
            }
            
            is HomeUiState.Error -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = (uiState as HomeUiState.Error).message,
                        color = White,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { viewModel.retry() },
                        colors = ButtonDefaults.buttonColors(containerColor = NetflixRed)
                    ) {
                        Text("Retry", color = White)
                    }
                }
            }
            
            is HomeUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Featured movie banner
                    item {
                        FeaturedMovieBanner(
                            movie = featuredMovie,
                            onPlayClick = { /* Handle play click */ },
                            onInfoClick = { onMovieClick(featuredMovie) }
                        )
                    }
                    
                    // Popular movies row
                    item {
                        MovieRow(
                            title = "Popular on CineVibe",
                            movies = popularMovies,
                            onMovieClick = onMovieClick
                        )
                    }
                    
                    // Trending movies row
                    item {
                        MovieRow(
                            title = "Trending Now",
                            movies = trendingMovies,
                            onMovieClick = onMovieClick
                        )
                    }
                    
                    // Top Rated movies row
                    item {
                        MovieRow(
                            title = "Top Rated",
                            movies = topRatedMovies,
                            onMovieClick = onMovieClick
                        )
                    }
                    
                    // Upcoming movies row
                    item {
                        MovieRow(
                            title = "Coming Soon",
                            movies = upcomingMovies,
                            onMovieClick = onMovieClick
                        )
                    }

                    // Add spacing at the bottom
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedMovieBanner(
    movie: Movie,
    onPlayClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp)
    ) {
        // Movie poster
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(
                    // Properly construct the URL for backdrop images
                    if (movie.backdropPath != null)
                        ApiConstants.IMAGE_BASE_URL + ApiConstants.BACKDROP_SIZE + movie.backdropPath
                    else if (movie.posterPath != null)
                        ApiConstants.IMAGE_BASE_URL + ApiConstants.POSTER_SIZE + movie.posterPath
                    else
                        "https://via.placeholder.com/1280x720?text=${movie.title}"
                )
                .build(),
            contentDescription = "Featured movie poster",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.95f)
                        ),
                        startY = 300f
                    )
                )
        )
        
        // Info at the bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .padding(horizontal = 16.dp)
        ) {
            // Movie title
            Text(
                text = movie.title,
                color = White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Play button
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_play),
                        contentDescription = "Play",
                        tint = Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Play",
                        color = Black,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Info button
                Button(
                    onClick = onInfoClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkGray.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = "Info",
                        tint = White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "More Info",
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MovieRow(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(movies) { movie ->
                MoviePoster(
                    movie = movie,
                    onClick = { onMovieClick(movie) },
                    modifier = Modifier
                        .width(125.dp)
                        .height(187.dp)
                        .padding(end = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MoviePoster(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(
                    // Properly construct the URL for poster images
                    if (movie.posterPath != null)
                        ApiConstants.IMAGE_BASE_URL + ApiConstants.POSTER_SIZE + movie.posterPath
                    else
                        "https://via.placeholder.com/500x750?text=${movie.title}"
                )
                .build(),
            contentDescription = "Movie poster for ${movie.title}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
