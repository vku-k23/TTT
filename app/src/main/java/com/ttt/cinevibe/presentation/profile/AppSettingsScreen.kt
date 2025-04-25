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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ttt.cinevibe.R
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
                title = stringResource(R.string.app_settings),
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
            SectionHeader(title = stringResource(R.string.notifications))
            
            SettingsCard {
                SettingsSwitch(
                    title = stringResource(R.string.push_notifications),
                    subtitle = stringResource(R.string.push_notifications_description),
                    checked = notificationsEnabled,
                    onCheckedChange = viewModel::toggleNotifications
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionHeader(title = stringResource(R.string.downloads))
            
            SettingsCard {
                SettingsSwitch(
                    title = stringResource(R.string.download_wifi_only),
                    subtitle = stringResource(R.string.download_wifi_description),
                    checked = downloadOnWifiOnly,
                    onCheckedChange = viewModel::toggleDownloadOnWifiOnly
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionHeader(title = stringResource(R.string.playback))
            
            SettingsCard {
                SettingsSwitch(
                    title = stringResource(R.string.autoplay_next_episode),
                    subtitle = stringResource(R.string.autoplay_description),
                    checked = autoplayEnabled,
                    onCheckedChange = viewModel::toggleAutoplay
                )
                
                SettingsSwitch(
                    title = stringResource(R.string.subtitles),
                    subtitle = stringResource(R.string.subtitles_description),
                    checked = subtitlesEnabled,
                    onCheckedChange = viewModel::toggleSubtitles
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionHeader(title = stringResource(R.string.app_information))
            
            SettingsCard {
                ProfileInfoItem(
                    label = stringResource(R.string.app_version),
                    value = "1.0.0"
                )
                
                ProfileInfoItem(
                    label = stringResource(R.string.build_number),
                    value = "2025.04.25.1"
                )
            }
        }
    }
}