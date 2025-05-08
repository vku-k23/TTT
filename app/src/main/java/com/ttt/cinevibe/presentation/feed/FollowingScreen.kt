package com.ttt.cinevibe.presentation.feed

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.ttt.cinevibe.R
import com.ttt.cinevibe.domain.model.Review
import com.ttt.cinevibe.domain.model.User
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FollowingScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    onMovieClick: (Int) -> Unit = {}
) {
    val followingReviews by viewModel.followingReviews.collectAsState()
    val followingUsers by viewModel.followingUsers.collectAsState()
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
                        text = stringResource(R.string.following_desc),
                        color = LightGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Following users section
                item {
                    Text(
                        text = stringResource(R.string.user_activity),
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
                    ) {
                        items(followingUsers) { user ->
                            UserItem(user = user)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = DarkGray.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Following reviews section
                item {
                    Text(
                        text = stringResource(R.string.popular_reviews),
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(followingReviews) { review ->
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
    }
}

@Composable
fun UserItem(user: User) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.avatar)
                .build(),
            contentDescription = "User avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = user.displayName,
            color = White,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ReviewItem(
    review: Review,
    onMovieClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // User info and rating
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(review.userAvatar)
                    .build(),
                contentDescription = "User avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.userName,
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                
                Text(
                    text = formatDate(review.timestamp),
                    color = LightGray,
                    fontSize = 12.sp
                )
            }
            
            // Star rating
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = review.rating.toString(),
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
        
        // Movie title (clickable)
        Text(
            text = review.movieTitle,
            color = NetflixRed,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .clickable(onClick = onMovieClick)
        )
        
        // Review content
        Text(
            text = review.content,
            color = White,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        
        // Likes and comments
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Likes",
                tint = LightGray,
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = review.likes.toString(),
                color = LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp, end = 16.dp)
            )
            
            Icon(
                imageVector = Icons.Filled.Comment,
                contentDescription = "Comments",
                tint = LightGray,
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = review.comments.toString(),
                color = LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}