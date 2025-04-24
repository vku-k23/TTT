package com.ttt.cinevibe.presentation.navigation

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: Int,
    val unselectedIcon: Int
)