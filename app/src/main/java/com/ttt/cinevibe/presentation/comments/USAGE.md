# Comment Feature Usage Guide

## Overview

The Comment feature allows users to interact with movie reviews by adding, editing, and liking comments. This document explains how to integrate the comment feature into your screens.

## How to Use

### 1. Add the CommentsSection to a Review

To add the comments section to a movie review, simply include the `CommentsSection` component and pass the `reviewId`:

```kotlin
import com.ttt.cinevibe.presentation.comments.CommentsSection

// Inside your Composable
CommentsSection(reviewId = review.id)
```

The CommentsSection is already integrated into the ReviewItem component, so you don't need to add it manually if you're using ReviewItem.

### 2. Customize the Appearance

The comments section is collapsible by default. Users can click on the "Comments" header to expand or collapse the section.

### 3. Using the Comment Repository Directly

If you need to access the comment functionality directly:

```kotlin
class YourViewModel @Inject constructor(
    private val commentRepository: CommentRepository
) : ViewModel() {
    
    fun loadComments(reviewId: Long) {
        viewModelScope.launch {
            commentRepository.getReviewComments(reviewId, 0, 20).collect { result ->
                // Handle result
            }
        }
    }
    
    fun createComment(reviewId: Long, content: String) {
        viewModelScope.launch {
            commentRepository.createComment(reviewId, content).collect { result ->
                // Handle result
            }
        }
    }
    
    // And so on for other operations...
}
```

## Available Operations

The comments feature supports the following operations:

- **View comments**: Display all comments for a specific review
- **Add comment**: Post a new comment on a review
- **Edit comment**: Modify an existing comment (only for the comment author)
- **Delete comment**: Remove a comment (only for the comment author)
- **Like/unlike comment**: Toggle like status on a comment

## Comments Limitations

- Maximum length: The backend enforces a limit of 500 characters per comment
- Rate limiting: Users are limited to posting 10 comments per minute to prevent spam
- Nested comments: Currently not supported, all comments are at the same level

## Security Considerations

- Comment operations are authenticated: Users must be logged in
- The backend verifies user ownership for edit/delete operations
- Content moderation is performed server-side
