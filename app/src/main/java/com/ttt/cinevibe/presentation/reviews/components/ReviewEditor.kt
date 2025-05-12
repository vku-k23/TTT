package com.ttt.cinevibe.presentation.reviews.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttt.cinevibe.domain.model.MovieReview

@Composable
fun ReviewEditor(
    movieTitle: String,
    initialReview: MovieReview? = null,
    isSubmitting: Boolean = false,
    onSubmit: (rating: Int, content: String) -> Unit,
    onCancel: () -> Unit
) {
    val isEditing = initialReview != null
    val focusManager = LocalFocusManager.current
    
    // State for review content and rating
    var reviewContent by remember { mutableStateOf(initialReview?.content ?: "") }
    var selectedRating by remember { mutableStateOf(initialReview?.rating ?: 0) }
    
    // Error states
    var contentError by remember { mutableStateOf<String?>(null) }
    var ratingError by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with movie title and close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) "Edit your review for" else "Write a review for",
                style = MaterialTheme.typography.titleMedium
            )
            
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }
        
        // Movie title
        Text(
            text = movieTitle,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Rating selector
        Text(
            text = "Your rating",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            repeat(5) { index ->
                val starPosition = index + 1
                
                IconButton(
                    onClick = { selectedRating = starPosition },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (starPosition <= selectedRating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Star $starPosition",
                        tint = if (starPosition <= selectedRating) Color(0xFFFFC107) else Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        
        // Rating error
        if (ratingError != null) {
            Text(
                text = ratingError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Review content
        Text(
            text = "Your review",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = reviewContent,
            onValueChange = {
                reviewContent = it
                if (contentError != null && it.isNotBlank()) {
                    contentError = null
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            placeholder = { Text("Share your thoughts about the movie...") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            isError = contentError != null,
            supportingText = if (contentError != null) { { Text(contentError!!) } } else null,
            maxLines = 8
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Submit button
        Button(
            onClick = {
                // Validate input
                var isValid = true
                
                if (selectedRating == 0) {
                    ratingError = "Please select a rating"
                    isValid = false
                } else {
                    ratingError = null
                }
                
                if (reviewContent.isBlank()) {
                    contentError = "Please write a review"
                    isValid = false
                } else if (reviewContent.length < 5) {
                    contentError = "Your review is too short"
                    isValid = false
                } else {
                    contentError = null
                }
                
                if (isValid) {
                    onSubmit(selectedRating, reviewContent)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = if (isEditing) "Update Review" else "Submit Review",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}