package com.ttt.cinevibe.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.data.repository.UserConnectionRepository
import com.ttt.cinevibe.domain.model.MovieList
import com.ttt.cinevibe.domain.model.MovieReview
import com.ttt.cinevibe.domain.model.Resource
import com.ttt.cinevibe.domain.model.Review
import com.ttt.cinevibe.domain.model.User
import com.ttt.cinevibe.domain.repository.MovieReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val userConnectionRepository: UserConnectionRepository,
    private val movieReviewRepository: MovieReviewRepository
) : ViewModel() {

    // Following tab state
    private val _followingReviews = MutableStateFlow<List<Review>>(emptyList())
    val followingReviews: StateFlow<List<Review>> = _followingReviews
    
    private val _followingUsers = MutableStateFlow<List<User>>(emptyList())
    val followingUsers: StateFlow<List<User>> = _followingUsers
    
    // Discover tab state
    private val _popularReviews = MutableStateFlow<List<Review>>(emptyList())
    val popularReviews: StateFlow<List<Review>> = _popularReviews
    
    private val _trendingDiscussions = MutableStateFlow<List<Review>>(emptyList())
    val trendingDiscussions: StateFlow<List<Review>> = _trendingDiscussions
    
    // Movie Reviews tab state
    private val _movieReviews = MutableStateFlow<List<Review>>(emptyList())
    val movieReviews: StateFlow<List<Review>> = _movieReviews
    
    // Lists tab state
    private val _popularLists = MutableStateFlow<List<MovieList>>(emptyList())
    val popularLists: StateFlow<List<MovieList>> = _popularLists
    
    private val _userCreatedLists = MutableStateFlow<List<MovieList>>(emptyList())
    val userCreatedLists: StateFlow<List<MovieList>> = _userCreatedLists
    
    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // In real implementation, we'll load data from repositories
                loadFollowingData()
                loadDiscoverData()
                loadMockListsData() // Keep mock data for lists until API is ready
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun loadFollowingData() {
        viewModelScope.launch {
            try {
                // Load following users
                val followingDeferred = async {
                    userConnectionRepository.getFollowing(0, 20).collectLatest { result ->
                        when (result) {
                            is Resource.Success -> {
                                val users = result.data?.content?.map { connection ->
                                    User(
                                        id = connection.id.toInt(),
                                        username = connection.followingUid,
                                        displayName = connection.followingName,
                                        avatar = connection.followingProfileImageUrl ?: "",
                                        bio = "",
                                        followersCount = 0,
                                        followingCount = 0
                                    )
                                } ?: emptyList()
                                _followingUsers.value = users
                            }
                            is Resource.Error -> {
                                _error.value = result.message ?: "Error loading following users"
                            }
                            else -> {}
                        }
                    }
                }
                
                // Load reviews from following users
                val reviewsDeferred = async {
                    movieReviewRepository.getFollowingReviews(0, 20).collectLatest { result ->
                        when (result) {
                            is Resource.Success -> {
                                val reviews = result.data?.map { movieReview ->
                                    convertToReview(movieReview)
                                } ?: emptyList()
                                _followingReviews.value = reviews
                            }
                            is Resource.Error -> {
                                _error.value = result.message ?: "Error loading following reviews"
                            }
                            else -> {}
                        }
                    }
                }
                
                followingDeferred.await()
                reviewsDeferred.await()
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                // Fall back to mock data if API call fails
                _followingUsers.value = createMockUsers(8)
                _followingReviews.value = createMockReviews(5)
            }
        }
    }
    
    private suspend fun loadDiscoverData() {
        viewModelScope.launch {
            try {
                // Load popular reviews
                val popularDeferred = async {
                    movieReviewRepository.getPopularReviews(0, 10).collectLatest { result ->
                        when (result) {
                            is Resource.Success -> {
                                val reviews = result.data?.map { movieReview ->
                                    convertToReview(movieReview)
                                } ?: emptyList()
                                _popularReviews.value = reviews
                            }
                            is Resource.Error -> {
                                _error.value = result.message ?: "Error loading popular reviews"
                            }
                            else -> {}
                        }
                    }
                }
                
                // Load trending discussions
                val trendingDeferred = async {
                    movieReviewRepository.getTrendingReviews(0, 6).collectLatest { result ->
                        when (result) {
                            is Resource.Success -> {
                                val reviews = result.data?.map { movieReview ->
                                    convertToReview(movieReview)
                                } ?: emptyList()
                                _trendingDiscussions.value = reviews
                            }
                            is Resource.Error -> {
                                _error.value = result.message ?: "Error loading trending discussions"
                            }
                            else -> {}
                        }
                    }
                }
                
                popularDeferred.await()
                trendingDeferred.await()
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                // Fall back to mock data if API call fails
                _popularReviews.value = createMockReviews(10)
                _trendingDiscussions.value = createMockReviews(6)
            }
        }
    }
    
    private fun convertToReview(movieReview: MovieReview): Review {
        return Review(
            id = movieReview.id,
            userUid = movieReview.userProfile.uid,
            userName = movieReview.userProfile.displayName,
            userProfileImageUrl = movieReview.userProfile.avatarUrl,
            tmdbMovieId = movieReview.tmdbMovieId,
            movieTitle = movieReview.movieTitle ?: "Movie Title", // Use actual movie title from server or fallback to "Movie Title"
            rating = movieReview.rating,
            reviewText = movieReview.content,
            containsSpoilers = false, // Default to false if not provided
            likesCount = movieReview.likeCount,
            commentCount = 0, // This field might not be directly available
            createdAt = movieReview.createdAt,
            updatedAt = movieReview.updatedAt
        )
    }
    
    // Method to refresh data
    fun refreshData() {
        viewModelScope.launch {
            loadInitialData()
        }
    }
    
    private fun loadMockListsData() {
        // For now, we'll use mock data for the lists
        _popularLists.value = createMockLists(8)
        _userCreatedLists.value = createMockLists(4)
    }
    
    // Helper methods to create mock data
    private fun createMockReviews(count: Int): List<Review> {
        val reviews = mutableListOf<Review>()
        val movieTitles = listOf("Dune: Part Two", "Joker: Folie à Deux", "Avengers: Secret Wars", 
            "The Batman 2", "Furiosa", "Inside Out 2", "Deadpool & Wolverine", "Gladiator II")
        
        val dateFormatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val now = java.time.LocalDateTime.now()
        
        repeat(count) { i ->
            val daysAgo = java.time.LocalDateTime.now().minusDays(i.toLong())
            
            reviews.add(
                Review(
                    id = i.toLong(),
                    userUid = "user$i",
                    userName = "User $i",
                    userProfileImageUrl = "https://i.pravatar.cc/150?img=$i",
                    tmdbMovieId = (i % 8).toLong(),
                    movieTitle = movieTitles[i % 8],
                    rating = 3.5f + (i % 5) * 0.5f,
                    reviewText = "This is a review for ${movieTitles[i % 8]}. The film was ${if (i % 2 == 0) "amazing" else "disappointing"} and I ${if (i % 2 == 0) "highly recommend it" else "don't recommend it"}.",
                    containsSpoilers = i % 3 == 0,
                    likesCount = i * 5,
                    commentCount = i * 2,
                    createdAt = daysAgo.format(dateFormatter),
                    updatedAt = if (i % 2 == 0) daysAgo.plusHours(1).format(dateFormatter) else null
                )
            )
        }
        return reviews
    }
    
    private fun createMockUsers(count: Int): List<User> {
        val users = mutableListOf<User>()
        repeat(count) { i ->
            users.add(
                User(
                    id = i,
                    username = "user${i}",
                    displayName = "User ${i}",
                    avatar = "https://i.pravatar.cc/150?img=$i",
                    bio = "Movie enthusiast and film critic",
                    followersCount = i * 10,
                    followingCount = i * 5
                )
            )
        }
        return users
    }
    
    private fun createMockLists(count: Int): List<MovieList> {
        val lists = mutableListOf<MovieList>()
        val listNames = listOf("Best Sci-Fi Movies", "Oscar Winners", "Marvel Universe", 
            "DC Films", "Classic Westerns", "Horror Must-Watch", "Animation Gems", "James Bond Collection")
        
        repeat(count) { i ->
            lists.add(
                MovieList(
                    id = i,
                    userId = i % 5,
                    userName = "User${i % 5}",
                    title = listNames[i % 8],
                    description = "A curated list of ${listNames[i % 8]} that every cinema lover should watch.",
                    movieCount = 5 + (i % 10),
                    likes = i * 8,
                    isPublic = true,
                    createdAt = System.currentTimeMillis() - (i * 86400000) // days ago
                )
            )
        }
        return lists
    }
}