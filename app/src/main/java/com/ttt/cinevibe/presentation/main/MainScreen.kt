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
import com.ttt.cinevibe.presentation.navigation.Screens

@Composable
fun MainScreen(
    rootNavController: NavHostController // Add this parameter for the top-level navigation
) {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem(
            route = Screens.HOME_ROUTE,
            selectedIcon = R.drawable.ic_home_filled,
            unselectedIcon = R.drawable.ic_home_outlined
        ),
        BottomNavItem(
            route = Screens.NEW_HOT_ROUTE,
            selectedIcon = R.drawable.ic_new_hot_filled,
            unselectedIcon = R.drawable.ic_new_hot_outlined
        ),
        BottomNavItem(
            route = Screens.SEARCH_ROUTE,
            selectedIcon = R.drawable.ic_search_filled,
            unselectedIcon = R.drawable.ic_search_outlined
        ),
        BottomNavItem(
            route = Screens.DOWNLOADS_ROUTE,
            selectedIcon = R.drawable.ic_downloads_filled,
            unselectedIcon = R.drawable.ic_downloads_outlined
        ),
        BottomNavItem(
            route = Screens.PROFILE_ROUTE,
            selectedIcon = R.drawable.ic_profile_filled,
            unselectedIcon = R.drawable.ic_profile_outlined
        )
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                items = bottomNavItems
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavGraph(
                navController = navController,
                rootNavController = rootNavController // Pass the root NavController to NavGraph
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Only show bottom navigation on main screens, not detail screens
    val isMainScreen = currentDestination?.route?.startsWith(Screens.MOVIE_DETAIL_ROUTE) != true

    if (isMainScreen) {
        NavigationBar(
            containerColor = androidx.compose.ui.graphics.Color.Black,
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = if (selected) item.selectedIcon else item.unselectedIcon),
                            contentDescription = item.title,
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(if (selected) 32.dp else 28.dp) // Make selected icon larger
                        )
                    },
                    selected = selected,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = androidx.compose.ui.graphics.Color.White,
                        selectedTextColor = androidx.compose.ui.graphics.Color.White,
                        unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                        indicatorColor = androidx.compose.ui.graphics.Color.Transparent // Hide the indicator background
                    ),
                    onClick = {
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