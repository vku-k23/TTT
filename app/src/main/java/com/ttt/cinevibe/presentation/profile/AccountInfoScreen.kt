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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ttt.cinevibe.presentation.auth.AuthViewModel
import com.ttt.cinevibe.ui.theme.Black

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountInfoScreen(
    onBackPressed: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val email = authViewModel.getCurrentUserEmail() ?: "User"
    val userId = authViewModel.getCurrentUserId() ?: "Unknown"
    
    Scaffold(
        containerColor = Black,
        topBar = {
            ProfileTopBar(
                title = "Account Information",
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
            SettingsCard {
                ProfileInfoItem(
                    label = "Full Name",
                    value = profileViewModel.getUserFullName()
                )
                
                ProfileInfoItem(
                    label = "Email",
                    value = email
                )
                
                ProfileInfoItem(
                    label = "Account ID",
                    value = userId
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionHeader(title = "Subscription Information")
            
            SettingsCard {
                ProfileInfoItem(
                    label = "Account Type",
                    value = profileViewModel.getUserAccountType()
                )
                
                ProfileInfoItem(
                    label = "Subscription Date",
                    value = profileViewModel.getUserSubscriptionDate()
                )
                
                ProfileInfoItem(
                    label = "Next Billing Date",
                    value = profileViewModel.getUserNextBillingDate()
                )
                
                ProfileInfoItem(
                    label = "Payment Method",
                    value = profileViewModel.getUserPaymentMethod()
                )
            }
        }
    }
}