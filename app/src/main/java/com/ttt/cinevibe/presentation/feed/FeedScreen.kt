package com.ttt.cinevibe.presentation.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ttt.cinevibe.R
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White

enum class FeedTab(val route: String) {
    FOLLOWING("following"),
    DISCOVER("discover"),
    MOVIE_REVIEWS("movie-reviews"),
    LISTS("lists")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    onNavigateToMovieDetails: (Int) -> Unit = {}
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    val tabs = listOf(
        FeedTab.FOLLOWING to stringResource(R.string.following),
        FeedTab.DISCOVER to stringResource(R.string.discover),
        FeedTab.MOVIE_REVIEWS to stringResource(R.string.movie_reviews),
        FeedTab.LISTS to stringResource(R.string.lists)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Black)
        ) {
            
            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Black,
                contentColor = White,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                        height = 3.dp,
                        color = Color.Transparent
                    )
                }
            ) {
                tabs.forEachIndexed { index, (_, title) ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) White else LightGray,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                            )
                        }
                    )
                }
            }
            
            // Content based on selected tab
            when (selectedTabIndex) {
                0 -> FollowingScreen(onMovieClick = onNavigateToMovieDetails)
                1 -> DiscoverScreen(onMovieClick = onNavigateToMovieDetails)
                2 -> MovieReviewsScreen(onMovieClick = onNavigateToMovieDetails)
                3 -> ListsScreen(onMovieClick = onNavigateToMovieDetails)
            }
        }
    }
}