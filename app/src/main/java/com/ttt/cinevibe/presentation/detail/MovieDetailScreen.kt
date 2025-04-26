package com.ttt.cinevibe.presentation.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.ttt.cinevibe.R
import com.ttt.cinevibe.data.remote.ApiConstants
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White

@Composable
fun MovieDetailScreen(
    movie: Movie,
    onBackClick: () -> Unit
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
            // Hero header image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .zIndex(1f)
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
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Black
                                ),
                                startY = 150f,
                                endY = 450f
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
                        .clickable { /* Play trailer */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
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
                                imageVector = Icons.Default.Star,
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
            
            // Play button
            Button(
                onClick = { /* Handle play */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = White
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.play),
                    color = Black,
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
                    imageVector = Icons.Default.PlayArrow,
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
                // Movie description/overview
                Text(
                    text = movie.overview,
                    color = White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    maxLines = if (isOverviewExpanded) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { isOverviewExpanded = !isOverviewExpanded }
                )
                
                // Action buttons row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // My List button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { /* Add to list */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add to My List",
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.my_list),
                            color = LightGray,
                            fontSize = 12.sp
                        )
                    }
                    
                    // Rate button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { /* Rate */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rate",
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.rate),
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
                            imageVector = Icons.Default.Share,
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
                Text(
                    text = stringResource(R.string.more_like_this),
                    color = White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // This would ideally contain a grid of similar movies
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Placeholder for similar movies
                    // In a real app, you would iterate through related movies
                    SimilarMovieItem(
                        imageUrl = "https://via.placeholder.com/300x450?text=Similar+1",
                        title = "Snow White",
                        modifier = Modifier.weight(1f)
                    )
                    SimilarMovieItem(
                        imageUrl = "https://via.placeholder.com/300x450?text=Similar+2",
                        title = "Narnia",
                        modifier = Modifier.weight(1f)
                    )
                    SimilarMovieItem(
                        imageUrl = "https://via.placeholder.com/300x450?text=Similar+3",
                        title = "The Seventh Seal",
                        modifier = Modifier.weight(1f)
                    )
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
                Text(
                    text = stringResource(R.string.trailers_and_more),
                    color = White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Trailer thumbnail
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { /* Play trailer */ }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(
                                if (movie.backdropPath != null)
                                    ApiConstants.IMAGE_BASE_URL + ApiConstants.BACKDROP_SIZE + movie.backdropPath
                                else
                                    "https://via.placeholder.com/1280x720?text=Trailer"
                            )
                            .build(),
                        contentDescription = "Trailer thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Play button overlay
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Trailer",
                            tint = White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    // Trailer title
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.8f)
                                    ),
                                    startY = 0f,
                                    endY = 100f
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Official Trailer",
                            color = White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
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
fun SimilarMovieItem(
    imageUrl: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
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