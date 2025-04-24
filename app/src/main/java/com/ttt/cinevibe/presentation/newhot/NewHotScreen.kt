package com.ttt.cinevibe.presentation.newhot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.ttt.cinevibe.R
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White

@Composable
fun NewHotScreen(
    viewModel: NewHotViewModel = hiltViewModel(),
    onMovieClick: (Movie) -> Unit = {}
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Coming Soon", "Everyone's Watching")
    
    // Collect StateFlow values from ViewModel
    val comingSoonMovies by viewModel.comingSoonMovies.collectAsState()
    val everyoneWatchingMovies by viewModel.everyoneWatchingMovies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        // Title
        Text(
            text = "New & Hot",
            color = White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        
        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Black,
            contentColor = White,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                    height = 3.dp,
                    color = NetflixRed
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { 
                        Text(
                            text = title,
                            color = if (selectedTabIndex == index) White else LightGray,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        ) 
                    }
                )
            }
        }
        
        // Loading indicator
        if (isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator(color = NetflixRed)
            }
        }
        // Error message
        else if (errorMessage != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = errorMessage ?: "Something went wrong",
                    color = White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                
                TextButton(onClick = { viewModel.retry() }) {
                    Text(text = "Retry", color = NetflixRed)
                }
            }
        }
        // Content based on selected tab
        else {
            when (selectedTabIndex) {
                0 -> ComingSoonTab(
                    movies = comingSoonMovies,
                    onMovieClick = onMovieClick
                )
                1 -> EveryonesWatchingTab(
                    movies = everyoneWatchingMovies,
                    onMovieClick = onMovieClick
                )
            }
        }
    }
}

@Composable
fun ComingSoonTab(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 56.dp) // Space for bottom navigation
    ) {
        items(movies) { movie ->
            ComingSoonItem(movie = movie, onClick = { onMovieClick(movie) })
        }
    }
}

@Composable
fun ComingSoonItem(
    movie: Movie,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        // Release date
        val releaseDate = movie.releaseDate ?: "Coming Soon"
        val releaseParts = releaseDate.split("-")
        val month = when (releaseParts.getOrNull(1)?.toIntOrNull()) {
            1 -> "JAN"
            2 -> "FEB"
            3 -> "MAR"
            4 -> "APR"
            5 -> "MAY"
            6 -> "JUN"
            7 -> "JUL"
            8 -> "AUG"
            9 -> "SEP"
            10 -> "OCT"
            11 -> "NOV"
            12 -> "DEC"
            else -> "SOON"
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(50.dp)
            ) {
                Text(
                    text = month,
                    color = White,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = releaseParts.getOrNull(2) ?: "",
                    color = White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Movie details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = movie.title,
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Coming ${movie.releaseDate ?: "Soon"}",
                    color = LightGray,
                    fontSize = 14.sp
                )
            }
            
            // Bell icon for notifications
            IconButton(onClick = { /* Set reminder */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notifications),
                    contentDescription = "Remind Me",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Movie poster or thumbnail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(
                        if (movie.posterPath != null) 
                            "https://via.placeholder.com/800x450?text=${movie.title}" 
                        else 
                            "https://via.placeholder.com/800x450?text=${movie.title}"
                    )
                    .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Movie description
        Text(
            text = movie.overview,
            color = White,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DarkGray)
        )
    }
}

@Composable
fun EveryonesWatchingTab(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 56.dp) // Space for bottom navigation
    ) {
        items(movies) { movie ->
            EveryonesWatchingItem(movie = movie, onClick = { onMovieClick(movie) })
        }
    }
}

@Composable
fun EveryonesWatchingItem(
    movie: Movie,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clickable(onClick = onClick)
    ) {
        // Title and info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = movie.title,
                color = White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = movie.overview,
                color = LightGray,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Movie poster or thumbnail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(
                        if (movie.posterPath != null) 
                            "https://via.placeholder.com/800x450?text=${movie.title}" 
                        else 
                            "https://via.placeholder.com/800x450?text=${movie.title}"
                    )
                    .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // My List button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add to My List",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "My List",
                    color = LightGray,
                    fontSize = 12.sp
                )
            }
            
            // Share button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_share),
                    contentDescription = "Share",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "Share",
                    color = LightGray,
                    fontSize = 12.sp
                )
            }
            
            // Play button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_play),
                    contentDescription = "Play",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "Play",
                    color = LightGray,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(DarkGray.copy(alpha = 0.5f))
        )
    }
}