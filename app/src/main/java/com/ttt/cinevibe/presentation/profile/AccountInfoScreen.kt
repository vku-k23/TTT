package com.ttt.cinevibe.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ttt.cinevibe.R
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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        ProfileTopBar(
            title = stringResource(R.string.account_information),
            onBackPressed = onBackPressed
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            SettingsCard {
                ProfileInfoItem(
                    label = stringResource(R.string.full_name),
                    value = profileViewModel.getUserFullName()
                )
                
                ProfileInfoItem(
                    label = stringResource(R.string.email),
                    value = email
                )
                
                ProfileInfoItem(
                    label = stringResource(id = R.string.account_id),
                    value = userId
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SectionHeader(title = stringResource(id = R.string.subscription_information))
            
            SettingsCard {
                ProfileInfoItem(
                    label = stringResource(id = R.string.account_type),
                    value = profileViewModel.getUserAccountType()
                )
                
                ProfileInfoItem(
                    label = stringResource(id = R.string.subscription_date),
                    value = profileViewModel.getUserSubscriptionDate()
                )
                
                ProfileInfoItem(
                    label = stringResource(id = R.string.next_billing_date),
                    value = profileViewModel.getUserNextBillingDate()
                )
                
                ProfileInfoItem(
                    label = stringResource(id = R.string.payment_method),
                    value = profileViewModel.getUserPaymentMethod()
                )
            }
        }
    }
}