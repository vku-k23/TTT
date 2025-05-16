package com.ttt.cinevibe.presentation.detail

import android.widget.TextView
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.ttt.cinevibe.R
import com.ttt.cinevibe.data.remote.ApiConstants
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White
import com.ttt.cinevibe.utils.LocalAppLocale
import com.ttt.cinevibe.utils.LocaleConfigurationProvider
import java.util.Locale
import com.ttt.cinevibe.presentation.detail.HasReviewedState
import com.ttt.cinevibe.presentation.detail.ReviewOperationState

@Composable
fun MovieDetailScreen(
    viewModel: MovieDetailViewModel = hiltViewModel(),
    movieId: Int,
    onBackClick: () -> Unit,
    onNavigateToDetails: (Int) -> Unit = {}, // Added navigation callback
    onNavigateToReviews: (Long, String) -> Unit = { _, _ -> } // New navigation callback for reviews
) {
    // Get the current app locale from our composition local
    val appLocale = LocalAppLocale.current
    val context = LocalContext.current
    
    // Forcefully apply locale to ensure correct string resources
    SideEffect {
        LocaleConfigurationProvider.applyLocaleToContext(context, appLocale)
        Locale.setDefault(appLocale) // Set JVM default locale
    }
    
    val movieState by viewModel.movieState.collectAsState()
    val trailerState by viewModel.trailerState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val reviewOperationState by viewModel.reviewOperationState.collectAsState()
    val hasReviewedState by viewModel.hasReviewedState.collectAsState()
    
    // Dialog state
    var showRatingDialog by remember { mutableStateOf(false) }    // Handle review operation result
    LaunchedEffect(reviewOperationState) {
        android.util.Log.d("MovieDetailScreen", "Review operation state changed: $reviewOperationState")
        when (reviewOperationState) {
            is ReviewOperationState.Success -> {
                // Check if the review was deleted (userReview is now null but hasReviewed was true)
                val hasReviewed = (hasReviewedState as? HasReviewedState.Success)?.hasReviewed ?: false
                val wasDeleted = hasReviewed && viewModel.userReview.value == null
                
                val message = when {
                    wasDeleted -> "Review deleted successfully!"
                    hasReviewed -> "Review updated successfully!"
                    else -> "Review submitted successfully!"
                }
                
                android.util.Log.d("MovieDetailScreen", "Review operation successful, closing dialog. hasReviewed=$hasReviewed")
                // Show success message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                showRatingDialog = false
                
                // Check review status again to update UI
                movieState.movie?.let {
                    viewModel.checkIfUserReviewed(it.id.toLong())
                }
            }
            is ReviewOperationState.Error -> {
                // Show error message
                android.util.Log.e("MovieDetailScreen", "Review operation error: ${(reviewOperationState as ReviewOperationState.Error).message}")
                Toast.makeText(context, (reviewOperationState as ReviewOperationState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }
    
    // Fetch the movie details
    LaunchedEffect(movieId) {
        viewModel.getMovieById(movieId)
    }
      // Handle hasReviewedState changes
    LaunchedEffect(hasReviewedState) {
        when (hasReviewedState) {
            is HasReviewedState.Success -> {
                // Only show the toast if the dialog is open and we're not deliberately trying to edit
                val hasReviewed = (hasReviewedState as HasReviewedState.Success).hasReviewed
                
                if (hasReviewed && showRatingDialog && viewModel.userReview.value == null) {
                    // We're finding out the user already reviewed but we don't have the review content yet
                    // Close the dialog and show the message
                    showRatingDialog = false
                    Toast.makeText(context, "You've already reviewed this movie", Toast.LENGTH_SHORT).show()
                }
            }
            is HasReviewedState.Error -> {
                Toast.makeText(context, (hasReviewedState as HasReviewedState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> {} // Do nothing for Loading state
        }
    }    // Show rating dialog if requested
    if (showRatingDialog) {
        movieState.movie?.let { movie ->
            // Get user's existing review if they have one
            val existingReview = viewModel.userReview.collectAsState().value
            val hasReviewed = (hasReviewedState as? HasReviewedState.Success)?.hasReviewed ?: false
              RatingDialog(
                movieTitle = movie.title,
                isSubmitting = reviewOperationState is ReviewOperationState.Loading,
                onSubmit = { rating, content ->                    if (hasReviewed && existingReview != null) {
                        if (rating == 0f && content == "DELETE") {
                            // Special case for deletion
                            viewModel.deleteReview(existingReview.id)
                        } else {
                            // Update existing review
                            viewModel.updateReview(existingReview.id, rating.toInt(), content)
                        }
                    } else {
                        // Create new review
                        viewModel.createReview(movie.id.toLong(), rating.toInt(), content)
                    }
                },
                onDismiss = { showRatingDialog = false }
            )
        }
    }
    
    // Handle loading and error states
    when {
        movieState.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NetflixRed)
            }
        }
        
        movieState.error != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Black),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = movieState.error ?: "Unknown error",
                        color = White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.getMovieById(movieId) },
                        colors = ButtonDefaults.buttonColors(containerColor = NetflixRed)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
          movieState.movie != null -> {
            MovieDetailContent(
                movie = movieState.movie!!,
                viewModel = viewModel,
                trailerState = trailerState,
                isFavorite = isFavorite,
                hasReviewedState = hasReviewedState,
                showRatingDialog = { showRatingDialog = true },
                onBackClick = onBackClick,
                onPlayTrailerClick = { viewModel.playTrailerInPlace() },
                onCloseTrailerClick = { viewModel.stopTrailerInPlace() },
                onToggleFavorite = { viewModel.toggleFavoriteStatus() },
                onNavigateToDetails = onNavigateToDetails,
                onNavigateToReviews = onNavigateToReviews // Pass the reviews navigation callback
            )
        }
    }
}

@Composable
fun MovieDetailContent(
    movie: Movie,
    viewModel: MovieDetailViewModel,
    trailerState: TrailerState,
    isFavorite: Boolean,
    hasReviewedState: HasReviewedState,
    showRatingDialog: () -> Unit,
    onBackClick: () -> Unit,
    onPlayTrailerClick: () -> Unit,
    onCloseTrailerClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToReviews: (Long, String) -> Unit // Add reviews navigation callback
) {
    val scrollState = rememberScrollState()
    val headerAlpha by animateFloatAsState(
        targetValue = (1f - (scrollState.value / 1000f)).coerceIn(0f, 1f),
        label = "headerAlpha"
    )
    
    var isOverviewExpanded by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Black)
    ) {
        // Main content with scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Hero header image or video player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .zIndex(1f)
            ) {
                // If trailer is playing in-place, show the YouTube player
                if (trailerState.isPlayingInPlace && trailerState.videoKey != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // In-place YouTube video player
                        YoutubePlayer(
                            videoId = trailerState.videoKey!!,
                            playing = true,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Close button for the video
                        IconButton(
                            onClick = onCloseTrailerClick,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(40.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close Video",
                                tint = White
                            )
                        }
                    }
                } else {
                    // Regular movie backdrop image when not playing video
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
                        contentDescription = "Movie backdrop",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Gradient overlay to make bottom text readable
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.1f),
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Black
                                    ),
                                    startY = 380f,
                                    endY = 850f
                                )
                            )
                    )
                    
                    // Play button overlay
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { onPlayTrailerClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    // Title and info at the bottom of hero
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        // Movie title
                        Text(
                            text = movie.title,
                            color = White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // Movie metadata row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Year
                            Text(
                                text = movie.releaseDate?.split("-")?.firstOrNull() ?: "2025",
                                color = LightGray,
                                fontSize = 14.sp
                            )
                            
                            // Quality badge (HD)
                            Text(
                                text = "HD",
                                color = LightGray,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .border(
                                        width = 1.dp,
                                        color = LightGray,
                                        shape = RoundedCornerShape(2.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                            
                            // Duration
                            Text(
                                text = "1h 42m",
                                color = LightGray,
                                fontSize = 14.sp
                            )
                            
                            // Rating with star
                            if (movie.voteAverage > 0) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFD700), // Gold
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f", movie.voteAverage),
                                    color = Color(0xFFFFD700), // Gold
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            // Play button
            Button(
                onClick = { onPlayTrailerClick() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (trailerState.isPlayingInPlace) NetflixRed else White
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = if (trailerState.isPlayingInPlace) Icons.Filled.Close else Icons.Filled.PlayArrow,
                    contentDescription = if (trailerState.isPlayingInPlace) "Stop Trailer" else "Play",
                    tint = if (trailerState.isPlayingInPlace) White else Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (trailerState.isPlayingInPlace) 
                              stringResource(R.string.stop_trailer) 
                           else if (trailerState.isAvailable) 
                              stringResource(R.string.play_trailer) 
                           else 
                              stringResource(R.string.play),
                    color = if (trailerState.isPlayingInPlace) White else Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            // Download button
            Button(
                onClick = { /* Handle download */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGray.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Download",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.download),
                    color = White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
            
            // Synopsis (Overview)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                // Section title
                Text(
                    text = stringResource(R.string.synopsis),
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Movie description/overview
                if (!movie.overview.isNullOrEmpty()) {
                    Text(
                        text = movie.overview,
                        color = White,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        maxLines = if (isOverviewExpanded) Int.MAX_VALUE else 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clickable { isOverviewExpanded = !isOverviewExpanded }
                            .background(DarkGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                    
                    if (!isOverviewExpanded && movie.overview.length > 150) {
                        Text(
                            text = stringResource(R.string.read_more),
                            color = NetflixRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clickable { isOverviewExpanded = true }
                        )
                    }
                } else {
                    // No overview available
                    Text(
                        text = stringResource(R.string.no_synopsis_available),
                        color = LightGray,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .background(DarkGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .fillMaxWidth()
                    )
                }
                
                // Action buttons row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Favorite/My List button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onToggleFavorite() }
                    ) {
                        androidx.compose.material.icons.Icons.Filled.run {
                            Icon(
                                imageVector = if (isFavorite) 
                                    Icons.Filled.Favorite 
                                else 
                                    Icons.Filled.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavorite) NetflixRed else White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isFavorite) stringResource(R.string.remove) else stringResource(R.string.my_list),
                            color = if (isFavorite) NetflixRed else LightGray,
                            fontSize = 12.sp
                        )
                    }                    // Rate button
                    val context = LocalContext.current
                    val hasReviewed = (hasReviewedState as? HasReviewedState.Success)?.hasReviewed ?: false
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { 
                            viewModel.checkIfUserReviewed(movie.id.toLong())
                            if (hasReviewed) {
                                // If they already have a review, go straight to the dialog to edit it
                                showRatingDialog()
                            } else {
                                // Show the dialog to create a new review
                                showRatingDialog()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rate",
                            tint = if (hasReviewed) Color(0xFFFFD700) else White, // Gold color if already reviewed
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (hasReviewed) "Rated" else stringResource(R.string.rate),
                            color = if (hasReviewed) Color(0xFFFFD700) else LightGray,
                            fontSize = 12.sp
                        )
                    }
                    
                    // Reviews button (using Comment icon instead of RateReview)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { 
                            onNavigateToReviews(movie.id.toLong(), movie.title) 
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Comment,
                            contentDescription = "Reviews",
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Reviews",
                            color = LightGray,
                            fontSize = 12.sp
                        )
                    }
                    
                    // Share button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { /* Share */ }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.share),
                            color = LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Divider(
                color = DarkGray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // More Like This section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Section title
                    Text(
                        text = stringResource(R.string.more_like_this),
                        color = White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // Description text
                    Text(
                        text = stringResource(R.string.similar_movies_description),
                        color = LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                // LazyRow of similar movies - styled like the My List screen
                val similarMovies by viewModel.similarMovies.collectAsState()
                
                if (similarMovies.isEmpty()) {
                    // Show loading or placeholder state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = NetflixRed,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                } else {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        contentPadding = PaddingValues(end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(similarMovies) { movie ->
                            SimilarMovieItem(
                                imageUrl = if (movie.posterPath != null)
                                    ApiConstants.IMAGE_BASE_URL + ApiConstants.POSTER_SIZE + movie.posterPath
                                else
                                    "https://via.placeholder.com/300x450?text=${movie.title}",
                                title = movie.title,
                                movie = movie,
                                onClick = { onNavigateToDetails(movie.id) },
                                modifier = Modifier
                                    .width(125.dp)
                                    .height(190.dp)
                            )
                        }
                    }
                }
            }
            
            Divider(
                color = DarkGray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // Trailers & More section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Section header with count
                val videosList by viewModel.videosList.collectAsState()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.trailers_and_more),
                        color = White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // Show the count of available videos
                    if (videosList.isNotEmpty()) {
                        Text(
                            text = "${videosList.size} Videos",
                            color = LightGray,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // List of trailer items in a column format
                if (videosList.isEmpty()) {
                    // Show loading or "No trailers available" message
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkGray.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (trailerState.isAvailable) {
                            CircularProgressIndicator(
                                color = NetflixRed,
                                modifier = Modifier.size(40.dp)
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "No Trailers",
                                    tint = LightGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = stringResource(R.string.no_trailer_available),
                                    color = LightGray,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    // Featured video player (first item)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .padding(bottom = 16.dp)
                    ) {
                        YoutubePlayer(
                            videoId = videosList[0].key,
                            playing = false, // Don't autoplay in the view
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Play button overlay
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { viewModel.playVideo(videosList[0].key) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Play Trailer",
                                    tint = White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            
                            // Featured trailer title
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.6f)
                                            ),
                                            startY = 0f,
                                            endY = 100f
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = videosList[0].name,
                                        color = White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Text(
                                        text = "Featured",
                                        color = NetflixRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    
                    // Only display additional videos if there are more than one
                    if (videosList.size > 1) {
                        // Additional trailers header
                        Text(
                            text = "More Videos",
                            color = White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        // List additional trailers (skip the first one)
                        videosList.drop(1).forEach { video ->
                            TrailerItem(
                                video = video,
                                onClick = { videoKey -> viewModel.playVideo(videoKey) },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Add some space at the bottom
            Spacer(modifier = Modifier.height(80.dp))
        }
        
        // Back button at the top
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .align(Alignment.TopStart)
                .alpha(headerAlpha)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun FullScreenTrailerDialog(
    videoKey: String,
    isPlaying: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // YouTube player
            YoutubePlayer(
                videoId = videoKey,
                playing = isPlaying,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )
            
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun YoutubePlayer(
    videoId: String,
    playing: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Store the current videoId to detect actual changes
    val currentVideoId = remember { mutableStateOf(videoId) }
    
    // Use a stable key for the player to prevent frequent recreations
    val stableVideoKey = remember(videoId) { videoId }
    
    // Single instance of the player to be reused
    val playerViewRef = remember { mutableStateOf<YouTubePlayerView?>(null) }
    val playerRef = remember { mutableStateOf<YouTubePlayer?>(null) }
    
    // Handle player setup only once using a flag
    val isInitialized = remember { mutableStateOf(false) }
    
    // Update player state when playing prop changes
    LaunchedEffect(playing) {
        if (isInitialized.value) {
            playerRef.value?.let { player ->
                if (playing) {
                    player.play()
                } else {
                    player.pause()
                }
            }
        }
    }
    
    // Monitor lifecycle to properly pause/resume playback
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    playerRef.value?.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (playing) {
                        playerRef.value?.play()
                    }
                }
                else -> {}
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Explicitly release resources
            playerViewRef.value?.release()
            playerViewRef.value = null
            playerRef.value = null
            isInitialized.value = false
        }
    }
    
    // Handle video ID changes
    LaunchedEffect(videoId) {
        if (isInitialized.value && currentVideoId.value != videoId) {
            playerRef.value?.cueVideo(videoId, 0f)
            currentVideoId.value = videoId
        }
    }
    
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            try {
                val playerView = YouTubePlayerView(ctx).apply {
                    // Disable automatic network observation to prevent TooManyRequestsException
                    enableAutomaticInitialization = false
                    
                    // Add a single observer manually
                    initialize(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            playerRef.value = youTubePlayer
                            youTubePlayer.cueVideo(stableVideoKey, 0f)
                            
                            if (playing) {
                                youTubePlayer.play()
                            }
                            
                            isInitialized.value = true
                            currentVideoId.value = videoId
                        }
                    }, false) // false = don't handle network automatically
                }
                
                // Store reference for cleanup
                playerViewRef.value = playerView
                playerView
            } catch (e: Exception) {
                // Fallback to TextView if YouTube player creation fails
                TextView(ctx).apply {
                    text = "Could not load video player"
                    setTextColor(android.graphics.Color.WHITE)
                    gravity = android.view.Gravity.CENTER
                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            }
        },
        update = { view ->
            // No need to do anything here, as we're handling updates in LaunchedEffect
        }
    )
}

@Composable
fun SimilarMovieItem(
    imageUrl: String,
    title: String,
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Text(
            text = title,
            color = LightGray,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun TrailerItem(
    video: VideoDetails,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkGray.copy(alpha = 0.3f))
            .clickable { onClick(video.key) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with play button
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(70.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Black)
        ) {
            // YouTube thumbnail
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://img.youtube.com/vi/${video.key}/0.jpg")
                    .build(),
                contentDescription = video.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Play button overlay
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // Video details
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            // Video title
            Text(
                text = video.name,
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Video type badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(DarkGray)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.type,
                    color = LightGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun RatingDialog(
    movieTitle: String,
    isSubmitting: Boolean,
    onSubmit: (Float, String) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableStateOf(0f) }
    var reviewText by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkGray,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "Rate & Review",
                    color = White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Movie title
                Text(
                    text = movieTitle,
                    color = LightGray,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                  // Rating bar
                com.ttt.cinevibe.presentation.components.RatingBar(
                    value = rating,
                    onValueChange = { rating = it },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    inactiveColor = LightGray.copy(alpha = 0.3f),
                    activeColor = NetflixRed
                )
                
                // Review text field
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = {
                        Text(
                            text = "Write your review...",
                            color = LightGray.copy(alpha = 0.7f)
                        )
                    },                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = White,
                        fontSize = 14.sp
                    ),                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = DarkGray,
                        unfocusedContainerColor = DarkGray,
                        focusedIndicatorColor = NetflixRed,
                        unfocusedIndicatorColor = LightGray.copy(alpha = 0.3f),
                        cursorColor = NetflixRed,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                
                // Anonymous review checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAnonymous,
                        onCheckedChange = { isAnonymous = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NetflixRed,
                            uncheckedColor = LightGray
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Submit as anonymous",
                        color = White,
                        fontSize = 14.sp
                    )
                }
                  // Submit button
                Button(                    onClick = {
                        if (rating > 0) {
                            onSubmit(rating, reviewText)
                        } else {
                            Toast.makeText(
                                context,
                                "Please select a rating",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NetflixRed
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isSubmitting) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(16.dp)
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.fillMaxSize()
                            )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "Submit Review",
                            color = White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}