package com.ttt.cinevibe.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ttt.cinevibe.ui.theme.Black

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val languages = viewModel.getAvailableLanguages()
    
    Scaffold(
        containerColor = Black,
        topBar = {
            ProfileTopBar(
                title = "Language Settings",
                onBackPressed = onBackPressed
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Black)
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            SectionHeader(title = "Select App Language")
            
            SettingsCard {
                languages.forEach { language ->
                    LanguageOption(
                        language = language,
                        isSelected = language == selectedLanguage,
                        onClick = { viewModel.setSelectedLanguage(language) }
                    )
                }
            }
        }
    }
}