package com.ttt.cinevibe.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttt.cinevibe.R
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onBackPressed: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Scaffold(
        containerColor = Black,
        topBar = {
            ProfileTopBar(
                title = stringResource(R.string.help_support),
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
            SectionHeader(title = stringResource(R.string.common_issues))
            
            SettingsCard {
                SupportItem(title = stringResource(R.string.streaming_quality_problems))
                SupportItem(title = stringResource(R.string.account_billing_questions))
                SupportItem(title = stringResource(R.string.app_not_working))
                SupportItem(title = stringResource(R.string.content_availability))
                SupportItem(title = stringResource(R.string.playback_issues))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionHeader(title = stringResource(R.string.contact_us))
            
            SettingsCard {
                SupportItem(title = stringResource(R.string.live_chat_support))
                SupportItem(title = stringResource(R.string.email_support))
                SupportItem(title = stringResource(R.string.call_center))
                SupportItem(title = stringResource(R.string.submit_feedback), isLastItem = true)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionHeader(title = stringResource(R.string.faq))
            
            SettingsCard {
                SupportItem(title = stringResource(R.string.account_management))
                SupportItem(title = stringResource(R.string.subscription_billing))
                SupportItem(title = stringResource(R.string.device_compatibility))
                SupportItem(title = stringResource(R.string.content_questions))
                SupportItem(title = stringResource(R.string.network_requirements), isLastItem = true)
            }
        }
    }
}

@Composable
fun SupportItem(
    title: String,
    isLastItem: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click action */ }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = White,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(R.string.view),
            tint = LightGray
        )
    }
    
    if (!isLastItem) {
        Divider(
            color = DarkGray.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
    }
}