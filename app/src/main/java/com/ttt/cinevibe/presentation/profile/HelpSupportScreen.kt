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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                title = "Help & Support",
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
            SectionHeader(title = "Common Issues")
            
            SettingsCard {
                SupportItem(title = "Streaming or Video Quality Problems")
                SupportItem(title = "Account & Billing Questions")
                SupportItem(title = "App Not Working Properly")
                SupportItem(title = "Content Availability")
                SupportItem(title = "Playback Issues")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionHeader(title = "Contact Us")
            
            SettingsCard {
                SupportItem(title = "Live Chat Support")
                SupportItem(title = "Email Support")
                SupportItem(title = "Call Center")
                SupportItem(title = "Submit Feedback", isLastItem = true)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionHeader(title = "FAQ")
            
            SettingsCard {
                SupportItem(title = "Account Management")
                SupportItem(title = "Subscription & Billing")
                SupportItem(title = "Device Compatibility")
                SupportItem(title = "Content Questions")
                SupportItem(title = "Network Requirements", isLastItem = true)
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
            contentDescription = "View",
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