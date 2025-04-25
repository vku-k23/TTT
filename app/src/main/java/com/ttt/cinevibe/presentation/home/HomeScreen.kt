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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    val continueWatchingMovies = viewModel.getRecentlyWatchedMovies() // Assuming this method exists or needs to be added
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
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Navigation Bar
                    TopNavigationBar()
                    
                    // Scrollable content
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Featured movie banner with tagline
                        item {
                            FeaturedMovieBanner(
                                movie = featuredMovie,
                                tagline = "BETRAYAL IS CLOSE", // Match the image
                                onPlayClick = { /* Handle play click */ },
                                onInfoClick = { onMovieClick(featuredMovie) }
                            )
                        }
                        
                        // Continue Watching section (as shown in the image)
                        item {
                            ContinueWatchingRow(
                                movies = continueWatchingMovies,
                                onMovieClick = onMovieClick
                            )
                        }
                        
                        // Popular movies row
                        item {
                            MovieRow(
                                title = stringResource(R.string.popular),
                                movies = popularMovies,
                                onMovieClick = onMovieClick
                            )
                        }
                        
                        // Trending movies row
                        item {
                            MovieRow(
                                title = stringResource(R.string.trending),
                                movies = trendingMovies,
                                onMovieClick = onMovieClick
                            )
                        }
                        
                        // Top Rated movies row
                        item {
                            MovieRow(
                                title = stringResource(R.string.top_rated),
                                movies = topRatedMovies,
                                onMovieClick = onMovieClick
                            )
                        }
                        
                        // Upcoming movies row
                        item {
                            MovieRow(
                                title = stringResource(R.string.upcoming),
                                movies = upcomingMovies,
                                onMovieClick = onMovieClick
                            )
                        }

                        // Add spacing at the bottom for navigation bar
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopNavigationBar() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("TV Shows", "Movies", "Categories")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Black)
            .padding(top = 8.dp)
    ) {
        // Netflix logo and navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "C",
                color = NetflixRed,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(end = 8.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Navigation tabs
            tabs.forEachIndexed { index, title ->
                Text(
                    text = title,
                    color = if (selectedTabIndex == index) White else White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clickable { selectedTabIndex = index }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun FeaturedMovieBanner(
    movie: Movie,
    tagline: String,
    onPlayClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp)
    ) {
        // Movie poster background
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
                            Color.Black.copy(alpha = 0.5f), // Darker at top for tagline text
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.95f) // Darker at bottom for buttons
                        ),
                        startY = 0f,
                        endY = 1000f
                    )
                )
        )
        
        // Tagline at the top (like "BETRAYAL IS CLOSE")
        Text(
            text = tagline,
            color = White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
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
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
            
            // Dot indicators for carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // Active dot
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(2.dp)
                        .background(NetflixRed)
                )
                
                // Inactive dots
                for (i in 1..4) {
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .width(16.dp)
                            .height(2.dp)
                            .background(White.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

@Composable
fun ContinueWatchingRow(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Continue Watching",
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
                ContinueWatchingItem(
                    movie = movie,
                    onClick = { onMovieClick(movie) },
                    modifier = Modifier
                        .width(200.dp)
                        .height(125.dp)
                        .padding(end = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ContinueWatchingItem(
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
                    if (movie.backdropPath != null)
                        ApiConstants.IMAGE_BASE_URL + ApiConstants.BACKDROP_SIZE + movie.backdropPath
                    else
                        "https://via.placeholder.com/400x225?text=${movie.title}"
                )
                .build(),
            contentDescription = "Movie thumbnail for ${movie.title}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Progress indicator bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(3.dp)
                .background(Color(0xFF333333))
        ) {
            Box(
                modifier = Modifier
                    .height(3.dp) // Use explicit height instead of fillMaxHeight
                    .width(140.dp) // Use fixed width instead of multiplying by modifier.width
                    .background(NetflixRed)
            )
        }
        
        // Play button overlay
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(40.dp)
                .height(40.dp) // Use width and height instead of size
                .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(50))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_play),
                contentDescription = "Play",
                tint = White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(24.dp)
                    .height(24.dp) // Use width and height instead of size
            )
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
