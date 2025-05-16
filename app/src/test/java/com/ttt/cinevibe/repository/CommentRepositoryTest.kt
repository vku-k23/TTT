package com.ttt.cinevibe.repository

import com.ttt.cinevibe.data.remote.api.CommentApiService
import com.ttt.cinevibe.data.remote.dto.*
import com.ttt.cinevibe.data.repository.CommentRepositoryImpl
import com.ttt.cinevibe.domain.model.Resource
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommentRepositoryTest {
    
    private lateinit var commentApiService: CommentApiService
    private lateinit var commentRepository: CommentRepositoryImpl
    
    @Before
    fun setup() {
        commentApiService = mock(CommentApiService::class.java)
        commentRepository = CommentRepositoryImpl(commentApiService)
    }
    
    @Test
    fun `getReviewComments returns Success with data when API call succeeds`() = runBlocking {
        // Arrange
        val reviewId = 1L
        val page = 0
        val size = 20
        val userProfileDto = UserProfileDto("user123", "Test User", "test@example.com", null)
        val commentDto = CommentDto(
            id = 1L,
            reviewId = reviewId,
            content = "Great review!",
            createdAt = "2025-05-16T12:00:00.000Z",
            updatedAt = "2025-05-16T12:00:00.000Z",
            likeCount = 5,
            userProfile = userProfileDto,
            userHasLiked = false
        )
        
        val pageable = PageableDto(0, 20, SortDto(true, false, true), 0, true, false)
        val pageResponse = CommentsPageResponse(
            content = listOf(commentDto),
            pageable = pageable,
            last = true,
            totalPages = 1,
            totalElements = 1,
            first = true,
            size = 20,
            number = 0,
            sort = SortDto(true, false, true),
            numberOfElements = 1,
            empty = false
        )
        
        `when`(commentApiService.getReviewComments(reviewId, page, size)).thenReturn(pageResponse)
        
        // Act
        val result = commentRepository.getReviewComments(reviewId, page, size).toList()
        
        // Assert
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(1, (result[1] as Resource.Success).data?.size)
        assertEquals("Great review!", (result[1] as Resource.Success).data?.first()?.content)
    }
    
    @Test
    fun `getReviewComments returns Error when API call fails`() = runBlocking {
        // Arrange
        val reviewId = 1L
        val page = 0
        val size = 20
        
        `when`(commentApiService.getReviewComments(reviewId, page, size)).thenThrow(IOException("Network error"))
        
        // Act
        val result = commentRepository.getReviewComments(reviewId, page, size).toList()
        
        // Assert
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Error)
        assertEquals("Network error. Please check your connection.", (result[1] as Resource.Error).message)
    }
    
    @Test
    fun `createComment returns Success with data when API call succeeds`() = runBlocking {
        // Arrange
        val reviewId = 1L
        val content = "This is a test comment"
        val commentRequest = CommentRequest(reviewId, content)
        
        val userProfileDto = UserProfileDto("user123", "Test User", "test@example.com", null)
        val commentDto = CommentDto(
            id = 1L,
            reviewId = reviewId,
            content = content,
            createdAt = "2025-05-16T12:00:00.000Z",
            updatedAt = "2025-05-16T12:00:00.000Z",
            likeCount = 0,
            userProfile = userProfileDto,
            userHasLiked = false
        )
        
        val response = CommentResponse(true, null, commentDto)
        
        `when`(commentApiService.createComment(commentRequest)).thenReturn(response)
        
        // Act
        val result = commentRepository.createComment(reviewId, content).toList()
        
        // Assert
        assertTrue(result[0] is Resource.Loading)
        assertTrue(result[1] is Resource.Success)
        assertEquals(content, (result[1] as Resource.Success).data?.content)
    }
}
