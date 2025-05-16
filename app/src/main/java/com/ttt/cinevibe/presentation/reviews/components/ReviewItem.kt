package com.ttt.cinevibe.presentation.reviews.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.ttt.cinevibe.domain.model.MovieReview
import com.ttt.cinevibe.presentation.comments.CommentsSection

@Composable
fun ReviewItem(
    review: MovieReview,
    isCurrentUserAuthor: Boolean = false,
    onLikeClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with user info and options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User avatar and name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                    ) {
                        if (review.userProfile.avatarUrl != null) {
                            AsyncImage(
                                model = review.userProfile.avatarUrl,
                                contentDescription = "User avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Default avatar",
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.Center),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // User name and review date
                    Column {
                        Text(
                            text = review.userProfile.displayName.takeIf { it.isNotBlank() } ?: review.userProfile.uid.takeIf { it.isNotBlank() } ?: "Anonymous User",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = review.getFormattedDate(),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Rating display
                RatingDisplay(rating = review.rating)
                
                // Options menu if current user is author
                if (isCurrentUserAuthor) {
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = { 
                                    onEditClick()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit review"
                                    )
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = { 
                                    onDeleteClick()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete review"
                                    )
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Review content
            var expanded by remember { mutableStateOf(false) }
            val maxLines = if (expanded) Int.MAX_VALUE else 4
            
            if (review.content.isNotBlank()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = review.content,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Show "Read more" button if content is truncated 
                    // Use lineCount estimation based on characters and width
                    val approximateLineCount = (review.content.length / 40) + 1 // ~40 chars per line
                    
                    if (approximateLineCount > 4 && !expanded) {
                        Text(
                            text = "Read more",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { expanded = true }
                        )
                    } else if (expanded && approximateLineCount > 4) {
                        Text(
                            text = "Show less",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { expanded = false }
                        )
                    }
                }
            } else {
                Text(
                    text = "No review content provided",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
              // Like button and count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onLikeClick() }
            ) {
                Icon(
                    imageVector = if (review.userHasLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                    contentDescription = "Like",
                    tint = if (review.userHasLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${review.likeCount} ${if (review.likeCount == 1) "like" else "likes"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Comments section
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            
            // Add comments section
            com.ttt.cinevibe.presentation.comments.CommentsSection(
                reviewId = review.id
            )
        }
    }
}

@Composable
fun RatingDisplay(rating: Float) {
    val maxRating = 5
    
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text = "$rating",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                rating >= 4f -> Color(0xFF4CAF50)  // Green for good ratings
                rating >= 3f -> Color(0xFFFFC107)  // Yellow for average ratings
                else -> Color(0xFFF44336)  // Red for poor ratings
            }
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Rating",
            tint = when {
                rating >= 4f -> Color(0xFF4CAF50)  // Green for good ratings
                rating >= 3f -> Color(0xFFFFC107)  // Yellow for average ratings
                else -> Color(0xFFF44336)  // Red for poor ratings
            },
            modifier = Modifier.size(16.dp)
        )
    }
}