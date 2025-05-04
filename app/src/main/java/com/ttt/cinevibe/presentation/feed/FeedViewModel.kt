package com.ttt.cinevibe.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.domain.model.Review
import com.ttt.cinevibe.domain.model.MovieList
import com.ttt.cinevibe.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    // Add repositories and use cases here as needed
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
                // In a real app, these would be API calls
                // For now, we'll use mock data
                loadMockData()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun loadMockData() {
        // For demo purposes, create mock data
        // Following tab data
        _followingReviews.value = createMockReviews(5)
        _followingUsers.value = createMockUsers(8)
        
        // Discover tab data
        _popularReviews.value = createMockReviews(10)
        _trendingDiscussions.value = createMockReviews(6)
        
        // Movie Reviews tab data
        _movieReviews.value = createMockReviews(15)
        
        // Lists tab data
        _popularLists.value = createMockLists(8)
        _userCreatedLists.value = createMockLists(4)
    }
    
    // Helper methods to create mock data
    private fun createMockReviews(count: Int): List<Review> {
        val reviews = mutableListOf<Review>()
        val movieTitles = listOf("Dune: Part Two", "Joker: Folie à Deux", "Avengers: Secret Wars", 
            "The Batman 2", "Furiosa", "Inside Out 2", "Deadpool & Wolverine", "Gladiator II")
        
        repeat(count) { i ->
            reviews.add(
                Review(
                    id = i,
                    userId = i % 10,
                    userName = "User${i % 10}",
                    userAvatar = "https://i.pravatar.cc/150?img=${i % 10}",
                    movieId = i % 8,
                    movieTitle = movieTitles[i % 8],
                    rating = 3.5f + (i % 5) * 0.5f,
                    content = "This is a review for ${movieTitles[i % 8]}. The film was ${if (i % 2 == 0) "amazing" else "disappointing"} and I ${if (i % 2 == 0) "highly recommend it" else "don't recommend it"}.",
                    likes = i * 5,
                    comments = i * 2,
                    timestamp = System.currentTimeMillis() - (i * 86400000) // days ago
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