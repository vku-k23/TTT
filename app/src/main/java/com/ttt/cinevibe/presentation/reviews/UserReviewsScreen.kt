package com.ttt.cinevibe.presentation.reviews

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReviewsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMovie: (Long) -> Unit,
    viewModel: MovieReviewViewModel = hiltViewModel()
) {
    val userReviewsState by viewModel.userReviewsState.collectAsState()
    val reviewOperationState by viewModel.reviewOperationState.collectAsState()
    
    val currentUser = FirebaseAuth.getInstance().currentUser
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    
    // Dialog states
    var editingReview by remember { mutableStateOf<MovieReview?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var reviewToDelete by remember { mutableStateOf<Long?>(null) }
    var movieTitleForEditing by remember { mutableStateOf("") }
    
    // Load user reviews
    LaunchedEffect(Unit) {
        viewModel.getUserReviews(refresh = true)
    }
    
    // Handle operation state changes
    LaunchedEffect(reviewOperationState) {
        when (reviewOperationState) {
            is ReviewOperationState.Success -> {
                val type = (reviewOperationState as ReviewOperationState.Success).type
                
                when (type) {
                    OperationType.UPDATE -> {
                        viewModel.getUserReviews(refresh = true)
                        editingReview = null
                    }
                    OperationType.DELETE -> {
                        viewModel.getUserReviews(refresh = true)
                    }
                    else -> {}
                }
            }
            is ReviewOperationState.Error -> {
                // Show error message
                scope.launch {
                    val message = (reviewOperationState as ReviewOperationState.Error).message
                    // You can show a snackbar or toast here with the error message
                }
            }
            else -> {}
        }
    }
    
    // Monitor scroll position for pagination
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
            .collectLatest { visibleItems ->
                if (userReviewsState is MovieReviewsState.Success) {
                    val reviews = (userReviewsState as MovieReviewsState.Success).reviews
                    
                    if (reviews.isNotEmpty() && 
                        visibleItems.isNotEmpty() && 
                        visibleItems.last().index >= reviews.size - 3) {
                        // Load more when we're 3 items from the end
                        viewModel.getUserReviews()
                    }
                }
            }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "My Reviews") },
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
                        viewModel.updateReview(editingReview!!.id, rating, content)
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
}