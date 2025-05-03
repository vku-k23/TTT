package com.ttt.cinevibe.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Class that holds both top and bottom navigation selection states
 */
class NavigationState(
    val topNavTab: MutableState<TopNavigationTab>,
    val bottomNavRoute: MutableState<String>
)

/**
 * CompositionLocal provider for accessing navigation state throughout the app
 */
val LocalNavigationState = compositionLocalOf { 
    NavigationState(
        topNavTab = mutableStateOf(TopNavigationTab.MOVIES),
        bottomNavRoute = mutableStateOf(Screens.HOME_ROUTE)
    )
}

/**
 * Remember a persistent navigation state that survives configuration changes
 */
@Composable
fun rememberNavigationState(): NavigationState {
    val topNavTab = rememberSaveable { mutableStateOf(TopNavigationTab.MOVIES) }
    val bottomNavRoute = rememberSaveable { mutableStateOf(Screens.HOME_ROUTE) }
    
    return remember {
        NavigationState(
            topNavTab = topNavTab,
            bottomNavRoute = bottomNavRoute
        )
    }
}