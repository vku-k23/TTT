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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White

@Composable
fun FeedScreen(
    onMovieClick: (Int) -> Unit = {}
) {
    // State for feed
    var selectedTab by remember { mutableStateOf(FeedTab.FOLLOWING) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        // Feed Header
        FeedHeader(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
        
        // Feed Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Black)
        ) {
            when (selectedTab) {
                FeedTab.FOLLOWING -> FollowingFeed(onMovieClick)
                FeedTab.DISCOVER -> DiscoverFeed(onMovieClick)
            }
        }
    }
}

@Composable
fun FeedHeader(
    selectedTab: FeedTab,
    onTabSelected: (FeedTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Black)
    ) {
        Text(
            text = "Feed",
            color = White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        
        // Feed tabs
        FeedTabs(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )
    }
}

@Composable
fun FeedTabs(
    selectedTab: FeedTab,
    onTabSelected: (FeedTab) -> Unit
) {
    androidx.compose.material3.TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = Black,
        contentColor = NetflixRed,
        indicator = { tabPositions ->
            androidx.compose.material3.TabRowDefaults.Indicator(
                modifier = Modifier.padding(horizontal = 16.dp),
                height = 2.dp,
                color = NetflixRed
            )
        }
    ) {
        FeedTab.values().forEach { tab ->
            androidx.compose.material3.Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.title,
                        fontSize = 14.sp,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selectedContentColor = NetflixRed,
                unselectedContentColor = LightGray
            )
        }
    }
}

@Composable
fun FollowingFeed(
    onMovieClick: (Int) -> Unit
) {
    // Placeholder for following feed content
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Here we would render user posts from people we follow
        // Displaying placeholder for now
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Following Feed Content\nReviews from users you follow will appear here",
                    color = White,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
fun DiscoverFeed(
    onMovieClick: (Int) -> Unit
) {
    // Placeholder for discover feed content
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Here we would render trending or popular posts
        // Displaying placeholder for now
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Discover Feed Content\nPopular reviews and trending discussions",
                    color = White,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

enum class FeedTab(val title: String) {
    FOLLOWING("Following"),
    DISCOVER("Discover")
}