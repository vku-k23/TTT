package com.ttt.cinevibe.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        // Notifications Header
        Text(
            text = "Notifications",
            color = White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        
        // Notifications Content
        NotificationsList()
    }
}

@Composable
fun NotificationsList() {
    // In a real app, this would come from a ViewModel
    val notifications = listOf(
        NotificationItem(
            id = 1,
            type = NotificationType.FOLLOW_REQUEST,
            senderName = "Alex Johnson",
            message = "wants to follow you",
            timestamp = Date(System.currentTimeMillis() - 3600000), // 1 hour ago
            isRead = false
        ),
        NotificationItem(
            id = 2,
            type = NotificationType.FOLLOW_ACCEPTED,
            senderName = "Maria Smith",
            message = "accepted your follow request",
            timestamp = Date(System.currentTimeMillis() - 86400000), // 1 day ago
            isRead = true
        ),
        NotificationItem(
            id = 3,
            type = NotificationType.LIKE,
            senderName = "David Brown",
            message = "liked your review of \"Inception\"",
            timestamp = Date(System.currentTimeMillis() - 259200000), // 3 days ago
            isRead = true
        ),
        NotificationItem(
            id = 4,
            type = NotificationType.COMMENT,
            senderName = "Sarah Williams",
            message = "commented on your review: \"Great analysis!\"",
            timestamp = Date(System.currentTimeMillis() - 604800000), // 1 week ago
            isRead = true
        ),
        NotificationItem(
            id = 5,
            type = NotificationType.RECOMMEND,
            senderName = "CineVibe",
            message = "We think you might enjoy \"The Matrix\" based on your watch history",
            timestamp = Date(System.currentTimeMillis() - 1209600000), // 2 weeks ago
            isRead = true
        )
    )
    
    if (notifications.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No notifications yet",
                color = LightGray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(notifications.size) { index ->
                NotificationItem(notification = notifications[index])
                
                if (index < notifications.size - 1) {
                    Divider(
                        color = DarkGray.copy(alpha = 0.5f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationItem) {
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle notification click */ }
            .background(if (!notification.isRead) DarkGray.copy(alpha = 0.2f) else Black)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar or icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Notification content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.senderName,
                    color = White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = notification.message,
                    color = LightGray,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = dateFormat.format(notification.timestamp),
                color = LightGray,
                fontSize = 12.sp
            )
        }
        
        // Unread indicator
        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(NetflixRed)
            )
        }
    }
}

data class NotificationItem(
    val id: Int,
    val type: NotificationType,
    val senderName: String,
    val message: String,
    val timestamp: Date,
    val isRead: Boolean = false
)

enum class NotificationType {
    FOLLOW_REQUEST,
    FOLLOW_ACCEPTED,
    LIKE,
    COMMENT,
    RECOMMEND
}