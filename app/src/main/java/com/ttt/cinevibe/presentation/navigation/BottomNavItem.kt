package com.ttt.cinevibe.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val title: String ?= null,
    @DrawableRes val selectedIcon: Int = 0,
    @DrawableRes val unselectedIcon: Int = 0,
    val selectedImageVector: ImageVector? = null,
    val unselectedImageVector: ImageVector? = null,
    val useImageVector: Boolean = false
)