package com.ttt.cinevibe.presentation.comments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ttt.cinevibe.domain.model.Comment
import com.ttt.cinevibe.domain.model.UserProfile
import com.ttt.cinevibe.presentation.comments.components.CommentEditor
import com.ttt.cinevibe.presentation.comments.components.CommentItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentDemoScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comment Feature Demo") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // A sample comment
            CommentItem(
                comment = Comment(
                    id = 1L,
                    reviewId = 1L,
                    content = "This is a great movie review! I totally agree with your thoughts about the cinematography.",
                    createdAt = "2025-05-16T10:30:00.000Z",
                    updatedAt = "2025-05-16T10:30:00.000Z",
                    likeCount = 5,
                    userProfile = UserProfile(
                        uid = "user123",
                        displayName = "John Doe",
                        email = "john@example.com",
                        avatarUrl = null
                    ),
                    userHasLiked = false
                ),
                isCurrentUserAuthor = true,
                onLikeClick = { },
                onEditClick = { },
                onDeleteClick = { }
            )
            
            // Comment editor
            CommentEditor(
                initialContent = "",
                onSubmit = { }
            )
            
            // Another comment
            CommentItem(
                comment = Comment(
                    id = 2L,
                    reviewId = 1L,
                    content = "I disagree with your rating. The movie deserves at least 4 stars!",
                    createdAt = "2025-05-15T14:20:00.000Z",
                    updatedAt = "2025-05-15T14:20:00.000Z",
                    likeCount = 2,
                    userProfile = UserProfile(
                        uid = "user456",
                        displayName = "Jane Smith",
                        email = "jane@example.com",
                        avatarUrl = null
                    ),
                    userHasLiked = true
                ),
                isCurrentUserAuthor = false,
                onLikeClick = { },
                onEditClick = { },
                onDeleteClick = { }
            )
        }
    }
}
