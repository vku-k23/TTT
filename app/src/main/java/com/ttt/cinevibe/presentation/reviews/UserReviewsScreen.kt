package com.ttt.cinevibe.presentation.reviews

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.ttt.cinevibe.domain.model.MovieReview
import com.ttt.cinevibe.presentation.reviews.components.ReviewEditor
import com.ttt.cinevibe.presentation.reviews.components.ReviewItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReviewsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMovie: (Long) -> Unit,
    userId: String? = null,
    viewModel: MovieReviewViewModel = hiltViewModel()
) {
    val userReviewsState by viewModel.userReviewsState.collectAsState()
    val reviewOperationState by viewModel.reviewOperationState.collectAsState()
    
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isCurrentUserProfile = userId == null || userId == currentUser?.uid
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val context = LocalContext.current
    
    // Dialog states
    var editingReview by remember { mutableStateOf<MovieReview?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var reviewToDelete by remember { mutableStateOf<Long?>(null) }
    var movieTitleForEditing by remember { mutableStateOf("") }
    
    // State for error handling
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Load user reviews based on userId
    LaunchedEffect(userId) {
        if (userId != null && userId != currentUser?.uid) {
            // Get another user's reviews
            viewModel.getUserReviewsByUserId(userId, true)
        } else {
            // Get current user's reviews
            viewModel.getUserReviews(refresh = true)
        }
    }
    
    // Handle operation state changes
    LaunchedEffect(reviewOperationState) {
        when (reviewOperationState) {
            is ReviewOperationState.Success -> {
                val type = (reviewOperationState as ReviewOperationState.Success).type
                
                when (type) {
                    OperationType.UPDATE -> {
                        if (isCurrentUserProfile) {
                            viewModel.getUserReviews(refresh = true)
                        } else if (userId != null) {
                            viewModel.getUserReviewsByUserId(userId, true)
                        }
                        // Always close the dialog after successful update
                        editingReview = null
                        Toast.makeText(context, "Review updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    OperationType.DELETE -> {
                        if (isCurrentUserProfile) {
                            viewModel.getUserReviews(refresh = true)
                        } else if (userId != null) {
                            viewModel.getUserReviewsByUserId(userId, true)
                        }
                        Toast.makeText(context, "Review deleted", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
            is ReviewOperationState.Error -> {
                // Show error message
                val message = (reviewOperationState as ReviewOperationState.Error).message
                errorMessage = message
                showErrorDialog = true
            }
            else -> {}
        }
    }
    
    // Monitor scroll position for pagination
    LaunchedEffect(Unit) {
        // Only set up the pagination observer once
        snapshotFlow { 
            if (lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty() &&
                userReviewsState is MovieReviewsState.Success) {
                val reviews = (userReviewsState as MovieReviewsState.Success).reviews
                val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()
                
                if (reviews.isNotEmpty() && lastVisibleItem != null && 
                    lastVisibleItem.index >= reviews.size - 3 &&
                    !lazyListState.isScrollInProgress) {
                    true  // Need to load more
                } else {
                    false // Don't need to load more
                }
            } else {
                false
            }
        }.collectLatest { shouldLoadMore ->
            if (shouldLoadMore) {
                // Load more when we're 3 items from the end
                if (isCurrentUserProfile) {
                    viewModel.getUserReviews()
                } else if (userId != null) {
                    viewModel.getUserReviewsByUserId(userId)
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isCurrentUserProfile) {
                            "My Reviews"
                        } else {
                            "User's Reviews"
                        }
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (userReviewsState) {
                is MovieReviewsState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MovieReviewsState.Success -> {
                    val reviews = (userReviewsState as MovieReviewsState.Success).reviews
                    
                    if (reviews.isEmpty()) {
                        // Empty state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "You haven't written any reviews yet",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Your movie reviews will appear here",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // List of reviews
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = lazyListState,
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(reviews) { review ->
                                ReviewItem(
                                    review = review,
                                    isCurrentUserAuthor = true,
                                    onLikeClick = { /* User shouldn't be able to like their own reviews */ },
                                    onEditClick = { 
                                        editingReview = review
                                        movieTitleForEditing = "this movie" // You could get the actual movie title if you have it
                                    },
                                    onDeleteClick = { 
                                        reviewToDelete = review.id
                                        showDeleteConfirmDialog = true
                                    }
                                )
                                
                                // Add clickable area to navigate to the movie
                                Spacer(modifier = Modifier.height(2.dp))
                                Button(
                                    onClick = { onNavigateToMovie(review.tmdbMovieId) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    content = { Text("View Movie") }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            // Footer loading indicator when loading more
                            if (userReviewsState is MovieReviewsState.LoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                is MovieReviewsState.Error -> {
                    // Error state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Oops! Something went wrong",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = (userReviewsState as MovieReviewsState.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(onClick = { viewModel.getUserReviews(true) }) {
                            Text(text = "Retry")
                        }
                    }
                }
                else -> { /* Initial state - nothing to display */ }
            }
        }
    }
    
    // Edit review dialog
    if (editingReview != null) {
        Dialog(
            onDismissRequest = { editingReview = null },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large
            ) {
                ReviewEditor(
                    movieTitle = movieTitleForEditing,
                    initialReview = editingReview,
                    isSubmitting = reviewOperationState is ReviewOperationState.Loading,
                    onSubmit = { rating, content ->
                        viewModel.updateReview(editingReview!!.id, rating, content, false)
                    },
                    onCancel = {
                        editingReview = null
                    }
                )
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmDialog && reviewToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmDialog = false
                reviewToDelete = null
            },
            title = { Text("Delete Review") },
            text = { Text("Are you sure you want to delete this review? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { 
                        reviewToDelete?.let { viewModel.deleteReview(it) }
                        showDeleteConfirmDialog = false
                        reviewToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteConfirmDialog = false
                        reviewToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Error dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { 
                Text(
                    if (errorMessage.contains("already reviewed", ignoreCase = true)) 
                        "Review Already Exists" 
                    else 
                        "Error"
                )
            },
            text = { 
                if (errorMessage.contains("already reviewed", ignoreCase = true)) {
                    Column {
                        Text("You've already reviewed this movie.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Please find your existing review in the list and update it instead of creating a new one.")
                    }
                } else {
                    Text(errorMessage)
                }
            },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}