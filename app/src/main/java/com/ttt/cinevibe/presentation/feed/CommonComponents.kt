package com.ttt.cinevibe.presentation.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.ttt.cinevibe.domain.model.Review
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White
import java.time.format.DateTimeFormatter
import java.util.Locale

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
                    .data(review.userProfileImageUrl)
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
                    text = formatDate(review.createdAt),
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
            text = review.reviewText,
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
                text = review.likesCount.toString(),
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
                text = review.commentCount.toString(),
                color = LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        // If date is already in a readable format, we can return it
        if (dateStr.contains("MMM") || dateStr.contains("Jan") || dateStr.contains("Feb")) {
            return dateStr
        }
        
        val inputFormats = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        )
        
        val outputFormat = DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())
        
        // Try each format until one works
        for (format in inputFormats) {
            try {
                val date = java.time.LocalDateTime.parse(dateStr, format)
                return outputFormat.format(date)
            } catch (e: Exception) {
                // Continue to next format
            }
        }
        
        // If parsing fails and we have a date with 'T', just take the date part
        if (dateStr.contains('T')) {
            dateStr.substringBefore('T')
        } else {
            dateStr
        }
    } catch (e: Exception) {
        // Return original if all else fails
        dateStr
    }
} 