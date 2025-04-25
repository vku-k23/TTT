package com.ttt.cinevibe.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ttt.cinevibe.ui.theme.Black

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    
    val notificationsEnabled by viewModel.notificationEnabled.collectAsState()
    val downloadOnWifiOnly by viewModel.downloadOnWifiOnly.collectAsState()
    val autoplayEnabled by viewModel.enableAutoplay.collectAsState()
    val subtitlesEnabled by viewModel.enableSubtitles.collectAsState()
    
    Scaffold(
        containerColor = Black,
        topBar = {
            ProfileTopBar(
                title = "App Settings",
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
            SectionHeader(title = "Notifications")
            
            SettingsCard {
                SettingsSwitch(
                    title = "Push Notifications",
                    subtitle = "Get notified about new releases and recommendations",
                    checked = notificationsEnabled,
                    onCheckedChange = viewModel::toggleNotifications
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionHeader(title = "Downloads")
            
            SettingsCard {
                SettingsSwitch(
                    title = "Download on Wi-Fi Only",
                    subtitle = "Save mobile data by downloading only on Wi-Fi",
                    checked = downloadOnWifiOnly,
                    onCheckedChange = viewModel::toggleDownloadOnWifiOnly
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionHeader(title = "Playback")
            
            SettingsCard {
                SettingsSwitch(
                    title = "Autoplay Next Episode",
                    subtitle = "Automatically play the next episode in a series",
                    checked = autoplayEnabled,
                    onCheckedChange = viewModel::toggleAutoplay
                )
                
                SettingsSwitch(
                    title = "Subtitles",
                    subtitle = "Show subtitles during playback",
                    checked = subtitlesEnabled,
                    onCheckedChange = viewModel::toggleSubtitles
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionHeader(title = "App Information")
            
            SettingsCard {
                ProfileInfoItem(
                    label = "App Version",
                    value = "1.0.0"
                )
                
                ProfileInfoItem(
                    label = "Build Number",
                    value = "2025.04.25.1"
                )
            }
        }
    }
}