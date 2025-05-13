package com.ttt.cinevibe.presentation.detail.components

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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun RatingDialog(
    movieTitle: String,
    isSubmitting: Boolean = false,
    initialRating: Float? = null,
    initialContent: String? = null,
    onSubmit: (rating: Float, content: String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
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
        ) {            ReviewEditorContent(
                movieTitle = movieTitle,
                isSubmitting = isSubmitting,
                onSubmit = onSubmit,
                onCancel = onDismiss,
                initialRating = initialRating,
                initialContent = initialContent
            )
        }
    }
}

@Composable
private fun ReviewEditorContent(
    movieTitle: String,
    isSubmitting: Boolean,
    onSubmit: (rating: Float, content: String) -> Unit,
    onCancel: () -> Unit,
    initialRating: Float? = null,
    initialContent: String? = null
) {
    val focusManager = LocalFocusManager.current
    val isEditing = initialRating != null || initialContent != null
    
    // State for review content and rating
    var reviewContent by remember { mutableStateOf(initialContent ?: "") }
    var selectedRating by remember { mutableStateOf(initialRating ?: 0f) }
    // Error states
    var contentError by remember { mutableStateOf<String?>(null) }
    var ratingError by remember { mutableStateOf<String?>(null) }
    
    // Validation function
    fun validate(): Boolean {
        var isValid = true
        
        if (selectedRating == 0f) {
            ratingError = "Please select a rating"
            isValid = false
        } else {
            ratingError = null
        }
        
        if (reviewContent.isBlank() && !isEditing) {
            // Only require content for new reviews
            contentError = "Please enter a review"
            isValid = false
        } else if (reviewContent.length in 1..4) {
            // Still validate length for both cases if content is provided
            contentError = "Review is too short"
            isValid = false
        } else {
            contentError = null
        }
        
        return isValid
    }
    
    // Automatically validate when in editing mode and fields change
    LaunchedEffect(selectedRating, reviewContent) {
        if (isEditing) {
            validate()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with title and close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {            Text(
                text = if(isEditing) "Edit your review" else "Rate this movie",
                style = MaterialTheme.typography.titleLarge
            )
            
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Movie title
        Text(
            text = movieTitle,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Star rating
        Text(
            text = "Your rating",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
          // Rating stars
        com.ttt.cinevibe.presentation.components.RatingBar(
            value = selectedRating,
            onValueChange = { selectedRating = it },
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            inactiveColor = Color.Gray,
            activeColor = Color(0xFFFFD700) // Gold color for selected stars
        )
        
        // Rating error
        if (ratingError != null) {
            Text(
                text = ratingError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Review content
        Text(
            text = "Your review",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
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
                .heightIn(min = 150.dp),
            placeholder = { Text("Share your thoughts about the movie...") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            isError = contentError != null,
            supportingText = contentError?.let { { Text(contentError!!) } }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
          // Submit button
        Button(
            onClick = {
                if (validate()) {
                    onSubmit(selectedRating, reviewContent)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = if(isEditing) "Update Review" else "Submit Review")
        }
          // Only show delete option if we're editing an existing review
        if (isEditing) {
            var showDeleteConfirmation by remember { mutableStateOf(false) }
            
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(MaterialTheme.colorScheme.error)
                )
            ) {
                Text(text = "Delete Review")
            }
            
            // Show confirmation dialog when needed
            if (showDeleteConfirmation) {
                DeleteConfirmationDialog(
                    onConfirm = {
                        // We use rating 0 as a special signal for deletion
                        onSubmit(0f, "DELETE")
                        showDeleteConfirmation = false
                    },
                    onDismiss = { showDeleteConfirmation = false }
                )
            }
        }
    }
}

/**
 * Dialog to confirm deletion of a review
 */
@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Review") },
        text = { 
            Text("Are you sure you want to delete your review? This action cannot be undone.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
