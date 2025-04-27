package com.ttt.cinevibe.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ttt.cinevibe.R
import com.ttt.cinevibe.presentation.navigation.BottomNavItem
import com.ttt.cinevibe.presentation.navigation.NavGraph
import com.ttt.cinevibe.presentation.navigation.NavigationState
import com.ttt.cinevibe.presentation.navigation.Screens
import com.ttt.cinevibe.presentation.navigation.TopNavigationTab
import com.ttt.cinevibe.presentation.navigation.rememberNavigationState
import androidx.compose.ui.graphics.Color

// Material Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person

@Composable
fun MainScreen(
    rootNavController: NavHostController
) {
    val navController = rememberNavController()
    val navigationState = rememberNavigationState()

    // Bottom navigation items (unchanged)
    val bottomNavItems = listOf(
        BottomNavItem(
            route = Screens.HOME_ROUTE,
            title = "Home",
            selectedImageVector = Icons.Filled.Home,
            unselectedImageVector = Icons.Outlined.Home,
            useImageVector = true
        ),
        BottomNavItem(
            route = Screens.NEW_HOT_ROUTE,
            title = "New & Hot",
            selectedImageVector = Icons.Filled.Tv,
            unselectedImageVector = Icons.Outlined.Tv,
            useImageVector = true
        ),
        BottomNavItem(
            route = Screens.SEARCH_ROUTE,
            title = "Search",
            selectedImageVector = Icons.Filled.Search,
            unselectedImageVector = Icons.Outlined.Search,
            useImageVector = true
        ),
        BottomNavItem(
            route = Screens.DOWNLOADS_ROUTE,
            title = "Downloads",
            selectedImageVector = Icons.Filled.Download,
            unselectedImageVector = Icons.Outlined.Download,
            useImageVector = true
        ),
        BottomNavItem(
            route = Screens.PROFILE_ROUTE,
            title = "Profile",
            selectedImageVector = Icons.Filled.Person,
            unselectedImageVector = Icons.Outlined.Person,
            useImageVector = true
        )
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                items = bottomNavItems,
                navigationState = navigationState // Pass the navigation state
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavGraph(
                navController = navController,
                rootNavController = rootNavController
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItem>,
    navigationState: NavigationState // Add navigation state parameter
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Only show bottom navigation on main screens, not detail screens
    val isMainScreen = currentDestination?.route?.startsWith(Screens.MOVIE_DETAIL_ROUTE) != true

    if (isMainScreen) {
        NavigationBar(
            containerColor = Color.Black,
        ) {
            items.forEach { item ->
                // Use the navigation state to determine selection
                val selected = navigationState.bottomNavRoute.value == item.route
                
                NavigationBarItem(
                    icon = {
                        if (item.useImageVector && item.selectedImageVector != null && item.unselectedImageVector != null) {
                            Icon(
                                imageVector = if (selected) item.selectedImageVector else item.unselectedImageVector,
                                contentDescription = item.title,
                                tint = Color.White,
                                modifier = Modifier.size(if (selected) 32.dp else 28.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = if (selected) item.selectedIcon else item.unselectedIcon),
                                contentDescription = item.title,
                                tint = Color.White,
                                modifier = Modifier.size(if (selected) 32.dp else 28.dp)
                            )
                        }
                    },
                    selected = selected,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent // Hide the indicator background
                    ),
                    onClick = {
                        // Update our navigation state
                        navigationState.bottomNavRoute.value = item.route
                        
                        // If we're on My List screen and click Home, reset top nav
                        if (item.route == Screens.HOME_ROUTE && currentDestination?.route == Screens.MY_LIST_ROUTE) {
                            navigationState.topNavTab.value = TopNavigationTab.MOVIES
                        }
                        
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}