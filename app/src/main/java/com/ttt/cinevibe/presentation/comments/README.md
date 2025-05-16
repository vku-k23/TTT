# Comment Feature for Movie Reviews

This document explains the implementation of the comment feature for movie reviews in the CineVibe Android application.

## Overview

The comment feature allows users to:
- View comments on movie reviews
- Add new comments to reviews
- Edit their own comments
- Delete their own comments
- Like/unlike comments

## Architecture

The comment feature follows the same Clean Architecture pattern used throughout the app:

1. **Data Layer**:
   - `CommentApiService`: Retrofit interface for API communication
   - `CommentDto`: Data transfer objects for network communication
   - `CommentRepositoryImpl`: Implementation of repository pattern

2. **Domain Layer**:
   - `Comment`: Domain model
   - `CommentRepository`: Repository interface
   - `Resource`: Generic wrapper for network responses

3. **Presentation Layer**:
   - `CommentViewModel`: Handles business logic and state management
   - `CommentsSection`: Main Compose UI component for displaying comments
   - `CommentItem`: Individual comment UI component
   - `CommentEditor`: UI component for creating/editing comments
   
## Key Implementation Details

### State Management

The comment feature uses StateFlow for reactive state management:

```kotlin
// States for comment data
sealed class CommentsState {
    object Loading : CommentsState()
    data class Success(val comments: List<Comment>) : CommentsState()
    data class Error(val message: String) : CommentsState()
}

// States for comment operations
sealed class CommentOperationState {
    object Idle : CommentOperationState()
    object Loading : CommentOperationState()
    data class Success(val message: String) : CommentOperationState()
    data class Error(val message: String) : CommentOperationState()
}
```

### UI Components

1. **CommentsSection**: A collapsible section showing comments for a review
   - Handles expanding/collapsing the comments list
   - Manages comment loading and pagination
   - Integrates comment editor for adding new comments

2. **CommentItem**: Individual comment display with:
   - User avatar and name
   - Comment content
   - Like button and count
   - Options menu for editing/deleting (for author only)

3. **CommentEditor**: Text input for adding/editing comments
   - Clean, modern design with rounded borders
   - Character limit indicator
   - Send button that activates when input is not empty

## Integration

The comment feature is integrated into the movie review UI:
- Each review displays a comment section underneath
- Comments can be expanded/collapsed
- Comment count is shown in the header

## API Endpoints

The feature communicates with the following backend endpoints:
- `GET /api/comments/review/{reviewId}`: Get comments for a review
- `GET /api/comments/{commentId}`: Get a specific comment
- `POST /api/comments`: Create a new comment
- `PUT /api/comments/{commentId}`: Update a comment
- `DELETE /api/comments/{commentId}`: Delete a comment
- `POST /api/comments/{commentId}/like`: Like a comment
- `DELETE /api/comments/{commentId}/like`: Unlike a comment

## Testing

The implementation includes:
- Unit tests for the repository implementation
- UI tests for the comment components

## Future Improvements

Potential enhancements:
- Add pagination for comments
- Add nested replies to comments
- Implement comment reporting functionality
- Add rich text formatting for comments
- Add image attachments
