package com.ttt.cinevibe.presentation.comments

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.ttt.cinevibe.domain.model.Comment
import com.ttt.cinevibe.presentation.comments.components.CommentEditor
import com.ttt.cinevibe.presentation.comments.components.CommentItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsSection(
    reviewId: Long,
    viewModel: CommentViewModel = hiltViewModel()
) {    val commentsState by viewModel.commentsState.collectAsState()
    val commentOperationState by viewModel.commentOperationState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    var expanded by remember { mutableStateOf(false) }
    var editingComment by remember { mutableStateOf<Comment?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<Long?>(null) }
    
    LaunchedEffect(reviewId) {
        viewModel.loadComments(reviewId)
    }
    
    LaunchedEffect(commentOperationState) {
        if (commentOperationState is CommentOperationState.Success) {
            viewModel.resetOperationState()
        }
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header and toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val commentCount = if (commentsState is CommentsState.Success) {
                (commentsState as CommentsState.Success).comments.size
            } else 0
            
            Text(
                text = "Comments ($commentCount)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }
          AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(
                initialHeight = { 0 },
                expandFrom = Alignment.Top,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = shrinkVertically(
                targetHeight = { 0 },
                shrinkTowards = Alignment.Top,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(durationMillis = 200))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Comment list
                when (commentsState) {
                    is CommentsState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is CommentsState.Success -> {
                        val comments = (commentsState as CommentsState.Success).comments
                        
                        if (comments.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No comments yet. Be the first to comment!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 350.dp)
                            ) {
                                items(comments) { comment ->
                                    CommentItem(
                                        comment = comment,
                                        isCurrentUserAuthor = viewModel.isCurrentUserAuthor(comment),
                                        onLikeClick = {
                                            if (comment.userHasLiked) {
                                                viewModel.unlikeComment(comment.id)
                                            } else {
                                                viewModel.likeComment(comment.id)
                                            }
                                        },
                                        onEditClick = {
                                            editingComment = comment
                                        },
                                        onDeleteClick = {
                                            commentToDelete = comment.id
                                            showDeleteConfirmDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is CommentsState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (commentsState as CommentsState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                // Add comment editor
                if (editingComment == null) {
                    CommentEditor(
                        onSubmit = { content ->
                            viewModel.createComment(reviewId, content)
                        }
                    )
                } else {
                    CommentEditor(
                        initialContent = editingComment?.content ?: "",
                        onSubmit = { content ->
                            editingComment?.id?.let { commentId ->
                                viewModel.updateComment(commentId, content)
                            }
                            editingComment = null
                        },
                        onCancel = {
                            editingComment = null
                        },
                        isEditing = true,
                        placeholder = "Edit your comment..."
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                commentToDelete = null
            },
            title = { Text("Delete Comment") },
            text = { Text("Are you sure you want to delete this comment? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        commentToDelete?.let { viewModel.deleteComment(it) }
                        showDeleteConfirmDialog = false
                        commentToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        commentToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Show snackbar for operation results
    when (val operation = commentOperationState) {
        is CommentOperationState.Error -> {
            LaunchedEffect(operation) {
                scope.launch {
                    SnackbarHostState().showSnackbar(
                        message = operation.message,
                        duration = SnackbarDuration.Short
                    )
                    viewModel.resetOperationState()
                }
            }
        }
        else -> {} // Handle other states as needed
    }
}
