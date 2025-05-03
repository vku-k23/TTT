package com.ttt.cinevibe.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

// Enum representing the top navigation tabs
enum class TopNavigationTab {
    MOVIES,
    TV_SHOWS,
    MY_LIST
}

// Composition local provider for the shared navigation state
val LocalTopNavigationState = compositionLocalOf { 
    mutableStateOf(TopNavigationTab.MOVIES) 
}

@Composable
fun <T> rememberTopNavigationState(): MutableState<T> where T : Enum<T> {
    return remember { 
        @Suppress("UNCHECKED_CAST")
        mutableStateOf(TopNavigationTab.MOVIES as T) 
    }
}