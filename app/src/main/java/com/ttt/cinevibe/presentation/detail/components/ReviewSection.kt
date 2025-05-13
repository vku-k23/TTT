package com.ttt.cinevibe.presentation.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.ttt.cinevibe.domain.model.MovieReview
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.White

@Composable
fun ReviewSection(
    reviews: List<MovieReview>,
    onAddReviewClick: () -> Unit,
    onViewAllReviewsClick: (Long, String) -> Unit,
    movieId: Long,
    movieTitle: String,
    hasReviewed: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Section header with "Reviews" and "See All" button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reviews",
                color = White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (reviews.isNotEmpty()) {
                TextButton(
                    onClick = { onViewAllReviewsClick(movieId, movieTitle) }
                ) {
                    Text(
                        text = "See All",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Reviews list (limited to 3)
        if (reviews.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No reviews yet",
                    color = LightGray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = onAddReviewClick,
                    enabled = !hasReviewed
                ) {
                    Text(
                        text = if (hasReviewed) "You've already reviewed" else "Be the first to review"
                    )
                }
            }
        } else {
            // Display up to 3 reviews
            Column(modifier = Modifier.fillMaxWidth()) {
                reviews.take(3).forEach { review ->
                    ReviewItem(review = review)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // If user hasn't reviewed yet, show add review button
                if (!hasReviewed) {
                    Button(
                        onClick = onAddReviewClick,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "Add Your Review")
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItem(
    review: MovieReview,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E1E1E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // User info and rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User avatar
                AsyncImage(
                    model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(review.userProfile.avatarUrl ?: "https://via.placeholder.com/40")
                        .build(),
                    contentDescription = "User avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // User name and rating
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = review.userProfile.displayName,
                        color = White,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "${review.rating}/10",
                            color = LightGray,
                            fontSize = 12.sp
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Date
                        Text(
                            text = review.createdAt.split("T")[0],
                            color = LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Review content
            Text(
                text = review.content,
                color = White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
} 