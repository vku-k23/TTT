package com.ttt.cinevibe.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttt.cinevibe.R
import com.ttt.cinevibe.presentation.navigation.TopNavigationTab
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White

@Composable
fun TopNavigationBar(
    selectedTab: TopNavigationTab,
    onTabSelected: (TopNavigationTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Black)
            .padding(top = 8.dp)
    ) {
        // Netflix logo and navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App logo - using a text "C" for CineVibe logo
            Text(
                text = "C",
                color = NetflixRed,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(end = 8.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Navigation tabs
            val tabs = listOf(
                TopNavigationTab.MOVIES to stringResource(R.string.movies),
                TopNavigationTab.TV_SHOWS to stringResource(R.string.tv_shows),
                TopNavigationTab.MY_LIST to stringResource(R.string.my_list)
            )
            
            // Create each tab with correct selection state
            tabs.forEach { (tabType, title) ->
                val isSelected = selectedTab == tabType
                Text(
                    text = title,
                    color = if (isSelected) White else White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clickable { onTabSelected(tabType) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}