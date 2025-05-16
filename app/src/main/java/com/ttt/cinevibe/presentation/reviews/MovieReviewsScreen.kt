package com.ttt.cinevibe.presentation.reviews

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.ttt.cinevibe.domain.model.MovieReview
import com.ttt.cinevibe.presentation.reviews.components.ReviewEditor
import com.ttt.cinevibe.presentation.reviews.components.ReviewItem
import com.ttt.cinevibe.ui.theme.NetflixRed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieReviewsScreen(
    movieId: Long,
    movieTitle: String,
    onNavigateBack: () -> Unit,
    viewModel: MovieReviewViewModel = hiltViewModel()
) {
    val movieReviewsState by viewModel.movieReviewsState.collectAsState()
    val reviewOperationState by viewModel.reviewOperationState.collectAsState()
    val hasReviewedState by viewModel.hasReviewedState.collectAsState()
    
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserUid = currentUser?.uid ?: ""
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    
    // Dialog states
    var showAddReviewDialog by remember { mutableStateOf(false) }
    var editingReview by remember { mutableStateOf<MovieReview?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var reviewToDelete by remember { mutableStateOf<Long?>(null) }
    
    // Load reviews and check if user has already reviewed
    LaunchedEffect(movieId) {
        viewModel.getMovieReviews(movieId)
        viewModel.checkIfUserReviewedMovie(movieId)
    }
    
    // Handle operation state changes
    LaunchedEffect(reviewOperationState) {
        when (reviewOperationState) {
            is ReviewOperationState.Success -> {
                val type = (reviewOperationState as ReviewOperationState.Success).type
                
                when (type) {
                    OperationType.CREATE, OperationType.UPDATE -> {
                        viewModel.getMovieReviews(movieId, refresh = true)
                        viewModel.checkIfUserReviewedMovie(movieId)
                        // Always close the dialog after a successful operation
                        showAddReviewDialog = false
                        editingReview = null
                        
                        // Show a toast to confirm the operation
                        val message = if (type == OperationType.CREATE) "Review posted successfully" else "Review updated successfully"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                    OperationType.DELETE -> {
                        viewModel.getMovieReviews(movieId, refresh = true)
                        viewModel.checkIfUserReviewedMovie(movieId)
                        Toast.makeText(context, "Review deleted", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
            is ReviewOperationState.Error -> {
                // Show error message
                scope.launch {
                    val message = (reviewOperationState as ReviewOperationState.Error).message
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
            else -> {}
        }
    }
    
    // Monitor scroll position for pagination - but in a more controlled way
    LaunchedEffect(Unit) {
        // Only set up the pagination observer once
        snapshotFlow { 
            if (lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty() &&
                movieReviewsState is MovieReviewsState.Success) {
                val reviews = (movieReviewsState as MovieReviewsState.Success).reviews
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
                viewModel.getMovieReviews(movieId)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Reviews") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Only show FAB if user hasn't reviewed yet
            if (hasReviewedState is HasReviewedState.Success && 
                !(hasReviewedState as HasReviewedState.Success).hasReviewed) {
                FloatingActionButton(
                    onClick = { showAddReviewDialog = true },
                    containerColor = NetflixRed
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add review"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (movieReviewsState) {
                is MovieReviewsState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MovieReviewsState.Success -> {
                    val reviews = (movieReviewsState as MovieReviewsState.Success).reviews
                    
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
                                text = "No reviews yet",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Be the first to share your thoughts!",
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
                                val isAuthor = review.userProfile.uid == currentUserUid
                                
                                ReviewItem(
                                    review = review,
                                    isCurrentUserAuthor = isAuthor,
                                    onLikeClick = { 
                                        if (review.userHasLiked) {
                                            viewModel.unlikeReview(review.id)
                                        } else {
                                            viewModel.likeReview(review.id)
                                        }
                                        // Update the like status locally to avoid a full refresh
                                        // We'll use a dedicated state to track likes in progress
                                    },
                                    onEditClick = { 
                                        editingReview = review
                                    },
                                    onDeleteClick = { 
                                        reviewToDelete = review.id
                                        showDeleteConfirmDialog = true
                                    }
                                )
                            }
                            
                            // Footer loading indicator when loading more
                            if (movieReviewsState is MovieReviewsState.LoadingMore) {
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
                            text = (movieReviewsState as MovieReviewsState.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(onClick = { viewModel.getMovieReviews(movieId, true) }) {
                            Text(text = "Retry")
                        }
                    }
                }
                else -> { /* Initial state - nothing to display */ }
            }
        }
    }
    
    // Add/Edit review dialog
    if (showAddReviewDialog || editingReview != null) {
        Dialog(
            onDismissRequest = { 
                showAddReviewDialog = false
                editingReview = null
            },
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
                    movieTitle = movieTitle,
                    initialReview = editingReview,
                    isSubmitting = reviewOperationState is ReviewOperationState.Loading,
                    onSubmit = { rating, content ->
                        if (editingReview != null) {
                            viewModel.updateReview(editingReview!!.id, rating, content, false)
                        } else {
                            viewModel.createReview(movieId, rating, content, movieTitle, false)
                        }
                    },
                    onCancel = {
                        showAddReviewDialog = false
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
}