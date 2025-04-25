package com.ttt.cinevibe.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onMovieClick: (Movie) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val featuredMovies by viewModel.featuredMovies.collectAsState()
    val currentFeaturedMovieIndex by viewModel.currentFeaturedMovieIndex.collectAsState()
    val selectedMovie by viewModel.selectedMovie.collectAsState()
    val isMovieDetailsVisible by viewModel.isMovieDetailsVisible.collectAsState()
    
    val continueWatchingMovies = viewModel.getRecentlyWatchedMovies()
    val popularMovies = viewModel.getPopularMovies()
    val topRatedMovies = viewModel.getTopRatedMovies()
    val trendingMovies = viewModel.getTrendingMovies()
    val upcomingMovies = viewModel.getUpcomingMovies()
    
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = currentFeaturedMovieIndex) {
        featuredMovies.size.coerceAtLeast(1)
    }
    
    // Keep pager in sync with view model
    LaunchedEffect(currentFeaturedMovieIndex) {
        if (featuredMovies.isNotEmpty() && currentFeaturedMovieIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(currentFeaturedMovieIndex)
        }
    }
    
    // Keep view model in sync with pager
    LaunchedEffect(pagerState.currentPage) {
        if (featuredMovies.isNotEmpty() && pagerState.currentPage != currentFeaturedMovieIndex) {
            viewModel.nextFeaturedMovie()
        }
    }
    
    // For debugging - log movie counts
    Log.d("HomeScreen", "Popular movies: ${popularMovies.size}")
    Log.d("HomeScreen", "Top rated movies: ${topRatedMovies.size}")
    Log.d("HomeScreen", "Trending movies: ${trendingMovies.size}")
    Log.d("HomeScreen", "Upcoming movies: ${upcomingMovies.size}")
    Log.d("HomeScreen", "Featured movies: ${featuredMovies.size}")
    
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
                        // Featured movie carousel with auto-slide
                        item {
                            FeaturedMovieCarousel(
                                movies = featuredMovies,
                                pagerState = pagerState,
                                onMovieClick = { viewModel.selectMovie(it) },
                                onPlayClick = { /* Handle play click */ },
                                onInfoClick = { movie -> viewModel.selectMovie(movie) },
                                onPreviousClick = {
                                    coroutineScope.launch {
                                        viewModel.previousFeaturedMovie()
                                    }
                                },
                                onNextClick = {
                                    coroutineScope.launch {
                                        viewModel.nextFeaturedMovie()
                                    }
                                }
                            )
                        }
                        
                        // Continue Watching section
                        item {
                            ContinueWatchingRow(
                                movies = continueWatchingMovies,
                                onMovieClick = { movie -> viewModel.selectMovie(movie) }
                            )
                        }
                        
                        // Popular movies row
                        item {
                            MovieRow(
                                title = stringResource(R.string.popular),
                                movies = popularMovies,
                                onMovieClick = { movie -> viewModel.selectMovie(movie) }
                            )
                        }
                        
                        // Trending movies row
                        item {
                            MovieRow(
                                title = stringResource(R.string.trending),
                                movies = trendingMovies,
                                onMovieClick = { movie -> viewModel.selectMovie(movie) }
                            )
                        }
                        
                        // Top Rated movies row
                        item {
                            MovieRow(
                                title = stringResource(R.string.top_rated),
                                movies = topRatedMovies,
                                onMovieClick = { movie -> viewModel.selectMovie(movie) }
                            )
                        }
                        
                        // Upcoming movies row
                        item {
                            MovieRow(
                                title = stringResource(R.string.upcoming),
                                movies = upcomingMovies,
                                onMovieClick = { movie -> viewModel.selectMovie(movie) }
                            )
                        }

                        // Add spacing at the bottom for navigation bar
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
                
                // Movie details overlay
                if (isMovieDetailsVisible && selectedMovie != null) {
                    MovieDetailsDialog(
                        movie = selectedMovie!!,
                        onDismiss = { viewModel.closeMovieDetails() },
                        onPlayClick = { /* Handle play click */ }
                    )
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
            // App logo - using a text "C" for CineVibe logo
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeaturedMovieCarousel(
    movies: List<Movie>,
    pagerState: PagerState,
    onMovieClick: (Movie) -> Unit = {},
    onPlayClick: () -> Unit = {},
    onInfoClick: (Movie) -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    if (movies.isEmpty()) {
        // Show placeholder if no movies
        FeaturedMovieBanner(
            movie = Movie(
                id = 0,
                title = "Loading Featured Movies",
                overview = "Please wait while we load featured content for you.",
                posterPath = null,
                backdropPath = null,
                releaseDate = "2025",
                voteAverage = 0.0
            ),
            tagline = "Coming Soon",
            onPlayClick = {},
            onInfoClick = {}
        )
        return
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp)
    ) {
        // Carousel content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val movie = movies.getOrNull(page) ?: return@HorizontalPager
            
            // Each carousel page/item
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onMovieClick(movie) }
            ) {
                // Movie backdrop image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            if (movie.backdropPath != null)
                                ApiConstants.IMAGE_BASE_URL + ApiConstants.BACKDROP_SIZE + movie.backdropPath
                            else if (movie.posterPath != null)
                                ApiConstants.IMAGE_BASE_URL + ApiConstants.POSTER_SIZE + movie.posterPath
                            else
                                "https://via.placeholder.com/1280x720?text=${movie.title}"
                        )
                        .build(),
                    contentDescription = "Featured movie poster for ${movie.title}",
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
                                    Color.Black.copy(alpha = 0.7f), // Darker at top for tagline
                                    Color.Transparent.copy(alpha = 0.1f), // Transparent in middle
                                    Color.Black.copy(alpha = 0.95f) // Darker at bottom for buttons
                                ),
                                startY = 0f,
                                endY = 1000f
                            )
                        )
                )
                
                // Tagline at the top
                val tagline = when (movie.title) {
                    "In the Lost Lands" -> "BETRAYAL IS CLOSE"
                    else -> "${movie.genres?.firstOrNull() ?: "NEW"} • ${movie.releaseDate?.split("-")?.firstOrNull()}"
                }
                
                Text(
                    text = tagline,
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 100.dp)
                )
                
                // Left navigation arrow
                IconButton(
                    onClick = onPreviousClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous movie",
                        tint = White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Right navigation arrow
                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next movie",
                        tint = White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Bottom area with title and buttons
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
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
                            onClick = { onInfoClick(movie) },
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
                    
                    // Carousel dot indicators
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(movies.size) { index ->
                            val isActive = index == pagerState.currentPage
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .width(if (isActive) 16.dp else 8.dp)
                                    .height(2.dp)
                                    .background(
                                        if (isActive) NetflixRed else White.copy(alpha = 0.5f)
                                    )
                            )
                        }
                    }
                }
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

@Composable
fun MovieDetailsDialog(
    movie: Movie,
    onDismiss: () -> Unit = {},
    onPlayClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(onClick = onDismiss)
    ) {
        // Movie details content in a bottom sheet similar to Netflix style shown in image
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f) // Set to approx 60% of screen height to match image
                    .align(Alignment.BottomCenter)
                    .background(Black)
                    .pointerInput(Unit) {
                        detectTapGestures { /* Prevent click from propagating to parent */ }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Movie poster (left side)
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(180.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(
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
                    
                    // Movie details (right side)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                    ) {
                        // Close button (X) in the top right corner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(36.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        // Movie title
                        Text(
                            text = movie.title,
                            color = White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // Release year
                        Text(
                            text = movie.releaseDate?.split("-")?.firstOrNull() ?: "2025",
                            color = LightGray,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        // Movie overview/description
                        Text(
                            text = movie.overview,
                            color = White,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                // Action buttons (Play, Download, Preview)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Play button
                    Button(
                        onClick = onPlayClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = White
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Play",
                            color = Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    
                    // Row with Download and Preview buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Download button
                        Button(
                            onClick = { /* Handle download */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            border = BorderStroke(1.dp, Color.White),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Download",
                                tint = White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Download",
                                color = White
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Preview button
                        Button(
                            onClick = { /* Handle preview */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            border = BorderStroke(1.dp, Color.White),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Preview",
                                tint = White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Preview",
                                color = White
                            )
                        }
                    }
                }
                
                // Details & More row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 16.dp)
                        .clickable { /* Handle details click */ },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Details & More",
                            color = White,
                            fontSize = 16.sp
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "View Details",
                        tint = White
                    )
                }
            }
        }
    }
}
