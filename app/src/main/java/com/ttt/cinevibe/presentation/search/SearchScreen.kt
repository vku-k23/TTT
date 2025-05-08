package com.ttt.cinevibe.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.ttt.cinevibe.data.remote.ApiConstants
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onMovieClick: (Movie) -> Unit = {}
) {
    val searchState by viewModel.searchState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        // Search bar with improved styling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .zIndex(1f)
        ) {
            TextField(
                value = viewModel.searchQuery.value,
                onValueChange = { query -> viewModel.onSearchQueryChanged(query) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                placeholder = { 
                    Text(
                        text = "Search for movies, genres, actors...",
                        color = LightGray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    if (viewModel.searchQuery.value.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.onSearchQueryChanged("")
                            focusManager.clearFocus()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = DarkGray,
                    focusedContainerColor = DarkGray,
                    unfocusedTextColor = White,
                    focusedTextColor = White,
                    cursorColor = White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(color = White, fontSize = 16.sp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        viewModel.performSearch()
                        focusManager.clearFocus()
                    }
                )
            )
        }
        
        // Content based on search state
        when (val state = searchState) {
            is SearchState.Initial -> {
                PopularSearchesSection(
                    categories = viewModel.getPopularCategories(),
                    onCategoryClick = { category ->
                        viewModel.onSearchQueryChanged(category)
                        viewModel.performSearch()
                        focusManager.clearFocus()
                    }
                )
            }
            is SearchState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NetflixRed)
                }
            }
            is SearchState.Success -> {
                if (state.movies.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No results found for '${viewModel.searchQuery.value}'",
                            color = White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    // Results found - show grid of movie posters
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.movies) { movie ->
                            SearchResultItem(
                                movie = movie,
                                onClick = { onMovieClick(movie) }
                            )
                        }
                    }
                }
            }
            is SearchState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Search Error",
                            color = White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = state.message,
                            color = Color.Red,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PopularSearchesSection(
    categories: List<String>,
    onCategoryClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Popular Categories",
            color = White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(categories) { category ->
                CategoryItem(
                    category = category,
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = category,
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SearchResultItem(
    movie: Movie,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .clickable(onClick = onClick)
    ) {
        // Movie poster
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f/3f),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
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
        
        // Movie title with truncation
        Text(
            text = movie.title,
            color = White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 2.dp)
        )
        
        // Rating
        if (movie.voteAverage > 0) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format("%.1f", movie.voteAverage),
                        color = LightGray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}