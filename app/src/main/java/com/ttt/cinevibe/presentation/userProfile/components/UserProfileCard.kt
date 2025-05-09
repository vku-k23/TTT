package com.ttt.cinevibe.presentation.userProfile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter

@Composable
fun UserProfileCard(
    userId: String,
    displayName: String,
    username: String,
    profileImageUrl: String?,
    bio: String?,
    connectionStatus: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            val painter = if (profileImageUrl.isNullOrEmpty()) {
                rememberVectorPainter(Icons.Default.Person)
            } else {
                rememberAsyncImagePainter(profileImageUrl)
            }
            
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Image(
                    painter = painter,
                    contentDescription = "Profile image of $displayName",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // User info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 4.dp)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!bio.isNullOrEmpty()) {
                    Text(
                        text = bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            // Connection status or action button
            connectionStatus?.let {
                val buttonText = when (it) {
                    "ACCEPTED" -> "Following"
                    "PENDING" -> "Requested"
                    else -> "Follow"
                }
                
                val isFollowing = it == "ACCEPTED"
                
                val buttonColors = when {
                    isFollowing -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                    else -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                OutlinedButton(
                    onClick = { /* Will be handled in profile screen */ },
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .height(36.dp),
                    colors = buttonColors,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = if (isFollowing) ButtonDefaults.outlinedButtonBorder else null
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
} 