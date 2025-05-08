package com.ttt.cinevibe.presentation.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
fun DiscoverScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    onMovieClick: (Int) -> Unit = {}
) {
    val popularReviews by viewModel.popularReviews.collectAsState()
    val trendingDiscussions by viewModel.trendingDiscussions.collectAsState()
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 56.dp) // Leave space for bottom navigation
            ) {
                item {
                    Text(
                        text = stringResource(R.string.discover_desc),
                        color = LightGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Popular Reviews section
                item {
                    Text(
                        text = stringResource(R.string.popular_reviews),
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(popularReviews.take(5)) { review ->
                    ReviewItem(
                        review = review,
                        onMovieClick = { onMovieClick(review.movieId) }
                    )
                    Divider(color = DarkGray.copy(alpha = 0.5f), thickness = 1.dp)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Trending Discussions section
                item {
                    Text(
                        text = stringResource(R.string.trending_discussions),
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(trendingDiscussions) { discussion ->
                    ReviewItem(
                        review = discussion,
                        onMovieClick = { onMovieClick(discussion.movieId) }
                    )
                    Divider(color = DarkGray.copy(alpha = 0.5f), thickness = 1.dp)
                }

                // Add some space at the bottom
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}