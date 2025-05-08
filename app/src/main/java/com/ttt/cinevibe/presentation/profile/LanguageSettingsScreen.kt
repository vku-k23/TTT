package com.ttt.cinevibe.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ttt.cinevibe.R
import com.ttt.cinevibe.ui.theme.Black
import kotlinx.coroutines.launch

@Composable
fun LanguageSettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val languages = viewModel.getAvailableLanguages()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            ProfileTopBar(
                title = stringResource(R.string.language_settings),
                onBackPressed = onBackPressed
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                SectionHeader(title = stringResource(R.string.select_app_language))
                
                SettingsCard {
                    languages.forEach { language ->
                        val isSelected = language == selectedLanguage
                        LanguageOption(
                            language = language,
                            isSelected = isSelected,
                            onClick = { 
                                if (!isSelected) {
                                    viewModel.setSelectedLanguage(language)
                                    // Show feedback to user
                                    scope.launch {
                                        snackbarHostState.showSnackbar(context.getString(R.string.changing_language_to, language))
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}