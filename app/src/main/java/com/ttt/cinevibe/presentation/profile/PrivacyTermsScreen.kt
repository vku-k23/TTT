
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
import com.ttt.cinevibe.presentation.profile.ProfileTopBar

@Composable
fun PrivacyTermsScreen(
    onBackPressed: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        ProfileTopBar(
            title = stringResource(R.string.privacy_terms),
            onBackPressed = onBackPressed
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.privacy_policy),
                color = White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.privacy_last_updated),
                color = LightGray,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PolicySection(
                title = stringResource(R.string.privacy_section_info_collect_title),
                content = stringResource(R.string.privacy_section_info_collect_content)
            )
            
            PolicySection(
                title = stringResource(R.string.privacy_section_info_use_title),
                content = stringResource(R.string.privacy_section_info_use_content)
            )
            
            PolicySection(
                title = stringResource(R.string.privacy_section_info_share_title),
                content = stringResource(R.string.privacy_section_info_share_content)
            )
            
            PolicySection(
                title = stringResource(R.string.privacy_section_choices_title),
                content = stringResource(R.string.privacy_section_choices_content)
            )
            
            PolicySection(
                title = stringResource(R.string.privacy_section_security_title),
                content = stringResource(R.string.privacy_section_security_content)
            )
            
            PolicySection(
                title = stringResource(R.string.privacy_section_contact_title),
                content = stringResource(R.string.privacy_section_contact_content)
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