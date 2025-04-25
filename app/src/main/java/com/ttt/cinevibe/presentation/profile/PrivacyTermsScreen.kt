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
fun PrivacyTermsScreen(
    onBackPressed: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Scaffold(
        containerColor = Black,
        topBar = {
            ProfileTopBar(
                title = "Privacy & Terms",
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
                text = "Privacy Policy",
                color = White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Last Updated: April 15, 2025",
                color = LightGray,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PolicySection(
                title = "Information We Collect",
                content = "We collect information you provide directly to us, such as when you create or modify your account, request customer support, or otherwise communicate with us. This information may include your name, email, password, postal address, phone number, payment method, and other information you choose to provide."
            )
            
            PolicySection(
                title = "How We Use Information",
                content = "We use the information we collect to provide, personalize, and improve our services, process your transactions, communicate with you about updates and promotions, protect against fraudulent or illegal activity, and for other purposes described in this privacy policy."
            )
            
            PolicySection(
                title = "Sharing Your Information",
                content = "We may share the information we collect with third parties who provide services on our behalf, such as payment processing, data analysis, email delivery, hosting services, and customer service. We may also share information with our business partners, affiliates, or in connection with a substantial corporate transaction."
            )
            
            PolicySection(
                title = "Your Choices",
                content = "You can modify your account information, update your communication preferences, or delete your account at any time. You may also opt-out of certain data collection practices by adjusting your device settings."
            )
            
            PolicySection(
                title = "Security",
                content = "We take reasonable measures to help protect the information we collect from loss, theft, misuse, and unauthorized access, disclosure, alteration, and destruction."
            )
            
            PolicySection(
                title = "Contact Us",
                content = "If you have any questions about this privacy policy, please contact us at privacy@cinevibe.com."
            )
        }
    }
}

@Composable
private fun PolicySection(
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