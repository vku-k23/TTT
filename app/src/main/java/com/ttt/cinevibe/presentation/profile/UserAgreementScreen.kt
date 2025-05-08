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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.White
import com.ttt.cinevibe.R

@Composable
fun UserAgreementScreen(
    onBackPressed: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        ProfileTopBar(
            title = stringResource(R.string.user_agreement),
            onBackPressed = onBackPressed
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.terms_of_service),
                color = White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.terms_effective_date),
                color = LightGray,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AgreementSection(
                title = stringResource(R.string.agreement_section_acceptance_title),
                content = stringResource(R.string.agreement_section_acceptance_content)
            )
            
            AgreementSection(
                title = stringResource(R.string.agreement_section_changes_title),
                content = stringResource(R.string.agreement_section_changes_content)
            )
            
            AgreementSection(
                title = stringResource(R.string.agreement_section_using_title),
                content = stringResource(R.string.agreement_section_using_content)
            )
            
            AgreementSection(
                title = stringResource(R.string.agreement_section_subscription_title),
                content = stringResource(R.string.agreement_section_subscription_content)
            )
            
            AgreementSection(
                title = stringResource(R.string.agreement_section_content_title),
                content = stringResource(R.string.agreement_section_content_content)
            )
            
            AgreementSection(
                title = stringResource(R.string.agreement_section_disclaimers_title),
                content = stringResource(R.string.agreement_section_disclaimers_content)
            )
            
            AgreementSection(
                title = stringResource(R.string.agreement_section_law_title),
                content = stringResource(R.string.agreement_section_law_content)
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