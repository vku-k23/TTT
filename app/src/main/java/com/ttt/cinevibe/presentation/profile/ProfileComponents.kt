package com.ttt.cinevibe.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(
    title: String,
    onBackPressed: () -> Unit
) {
    TopAppBar(
        title = { 
            Text(
                text = title, 
                color = White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ) 
        },
        navigationIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = White,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable { onBackPressed() }
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Black
        ),
        modifier = Modifier
            .fillMaxWidth(),
        windowInsets = WindowInsets(0,0,0,0)
    )
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = NetflixRed,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun ProfileInfoItem(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = LightGray,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            color = White,
            fontSize = 16.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (label != "Payment Method") { // Don't add divider after last item
            HorizontalDivider(
                color = Black.copy(alpha = 0.5f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun SettingsSwitch(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = White,
                fontSize = 16.sp
            )
            
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = subtitle,
                    color = LightGray,
                    fontSize = 12.sp
                )
            }
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = White,
                checkedTrackColor = NetflixRed,
                checkedBorderColor = NetflixRed,
                uncheckedThumbColor = White,
                uncheckedTrackColor = Black.copy(alpha = 0.7f),
                uncheckedBorderColor = LightGray
            )
        )
    }
    
    HorizontalDivider(
        color = Black.copy(alpha = 0.5f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun LanguageOption(
    language: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = language,
            color = White,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NetflixRed),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(White)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Black)
                    .padding(1.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LightGray.copy(alpha = 0.3f))
            )
        }
    }
    
    HorizontalDivider(
        color = Black.copy(alpha = 0.5f),
        thickness = 0.5.dp
    )
}