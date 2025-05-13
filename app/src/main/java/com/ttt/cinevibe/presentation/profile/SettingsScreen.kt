package com.ttt.cinevibe.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ttt.cinevibe.R
import com.ttt.cinevibe.presentation.auth.AuthState
import com.ttt.cinevibe.presentation.auth.AuthViewModel
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToAccountInfo: () -> Unit,
    onNavigateToAppSettings: () -> Unit,
    onNavigateToLanguageSettings: () -> Unit,
    onNavigateToHelpSupport: () -> Unit,
    onNavigateToPrivacyTerms: () -> Unit,
    onNavigateToUserAgreement: () -> Unit,
    onBackPressed: () -> Unit,
    onLogout: () -> Unit
) {
    val logoutState by authViewModel.logoutState.collectAsState()
    val scrollState = rememberScrollState()
    
    // Effect to handle logout state
    LaunchedEffect(key1 = logoutState) {
        when (logoutState) {
            is AuthState.Success -> {
                authViewModel.resetAuthStates()
                onLogout()
            }
            is AuthState.Error -> {
                authViewModel.resetAuthStates()
            }
            else -> {}
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        ProfileTopBar(
            title = stringResource(R.string.general_setting),
            onBackPressed = onBackPressed
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Menu Items
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfileMenuItem(
                    icon = Icons.Filled.Person,
                    title = stringResource(R.string.account_information),
                    onClick = onNavigateToAccountInfo
                )

                ProfileMenuItem(
                    icon = Icons.Filled.Settings,
                    title = stringResource(R.string.app_settings),
                    onClick = onNavigateToAppSettings
                )
                
                ProfileMenuItem(
                    icon = Icons.Filled.Language,
                    title = stringResource(R.string.change_language),
                    onClick = onNavigateToLanguageSettings
                )
                
                ProfileMenuItem(
                    icon = Icons.Filled.Info,
                    title = stringResource(R.string.help_support),
                    onClick = onNavigateToHelpSupport
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Logout Button
            Button(
                onClick = {
                    // First clear profile view model data
                    profileViewModel.clearUserData()
                    // Then trigger logout process
                    authViewModel.logout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NetflixRed,
                    contentColor = White
                ),
                shape = RoundedCornerShape(4.dp),
                enabled = logoutState !is AuthState.Loading
            ) {
                Text(
                    text = stringResource(R.string.logout),
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Footer Links
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.privacy_terms),
                    color = LightGray,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .clickable { onNavigateToPrivacyTerms() }
                )
                
                Text(
                    text = stringResource(R.string.user_agreement),
                    color = LightGray,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .clickable { onNavigateToUserAgreement() }
                )
            }
            
            // Show loading indicator if logout is in progress
            if (logoutState is AuthState.Loading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = NetflixRed
                )
            }
        }
    }
}