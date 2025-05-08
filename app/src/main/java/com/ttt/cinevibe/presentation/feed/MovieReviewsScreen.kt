package com.ttt.cinevibe.presentation.feed

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ttt.cinevibe.R
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White

@Composable
fun MovieReviewsScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    onMovieClick: (Int) -> Unit = {}
) {
    val movieReviews by viewModel.movieReviews.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Black)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = NetflixRed
            )
        } else if (error != null) {
            Text(
                text = error ?: "An unknown error occurred",
                color = Color.Red,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        } else if (movieReviews.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.no_reviews),
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.movie_reviews_desc),
                    color = LightGray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 56.dp) // Leave space for bottom navigation
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.movie_reviews_desc),
                            color = LightGray,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Movie reviews list
                items(movieReviews) { review ->
                    ReviewItem(
                        review = review,
                        onMovieClick = { onMovieClick(review.movieId) }
                    )
                    Divider(color = DarkGray.copy(alpha = 0.5f), thickness = 1.dp)
                }

                // Add some space at the bottom
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
        // Add review floating action button
        FloatingActionButton(
            onClick = { /* Handle write review */ },
            containerColor = NetflixRed,
            contentColor = White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.write_review)
            )
        }
    }
}