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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAgreementScreen(
    onBackPressed: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Scaffold(
        containerColor = Black,
        topBar = {
            ProfileTopBar(
                title = "User Agreement",
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
                .padding(16.dp)
        ) {
            Text(
                text = "Terms of Service",
                color = White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Effective Date: April 20, 2025",
                color = LightGray,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AgreementSection(
                title = "1. Acceptance of Terms",
                content = "By accessing or using CineVibe's services, you agree to be bound by these Terms of Service. If you do not agree to these terms, please do not use our service."
            )
            
            AgreementSection(
                title = "2. Changes to Terms",
                content = "CineVibe may modify these terms at any time. We will notify you of any changes by posting the updated terms on our website or app. Your continued use of our services after any changes indicates your acceptance of the modified terms."
            )
            
            AgreementSection(
                title = "3. Using Our Service",
                content = "You must be at least 13 years old or the minimum age required in your country to use our service. You agree to provide accurate information when registering and to keep this information updated. You are responsible for all activities that occur under your account."
            )
            
            AgreementSection(
                title = "4. Subscription and Billing",
                content = "Some aspects of our service require payment of fees. You will be charged on a recurring basis according to your subscription plan. All fees are non-refundable except as required by law or as specifically provided in these terms."
            )
            
            AgreementSection(
                title = "5. Content and Licenses",
                content = "All content available through our service is owned by CineVibe or its licensors and is protected by intellectual property rights. You agree not to reproduce, distribute, modify, display, or use the content in any way not expressly authorized by CineVibe."
            )
            
            AgreementSection(
                title = "6. Disclaimers and Limitations of Liability",
                content = "THE SERVICE AND ALL CONTENT ARE PROVIDED 'AS IS' WITHOUT WARRANTY OF ANY KIND. TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, CINEVIBE SHALL NOT BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL OR PUNITIVE DAMAGES."
            )
            
            AgreementSection(
                title = "7. Governing Law",
                content = "These terms shall be governed by the laws of the jurisdiction in which you reside, without regard to its conflict of law provisions."
            )
        }
    }
}

@Composable
private fun AgreementSection(
    title: String,
    content: String
) {
    Text(
        text = title,
        color = White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = content,
        color = LightGray,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    
    Spacer(modifier = Modifier.height(24.dp))
}