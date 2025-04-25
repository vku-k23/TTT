package com.ttt.cinevibe.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ttt.cinevibe.presentation.auth.AuthViewModel
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.MediumGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackPressed: () -> Unit,
    onSaveComplete: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val email = authViewModel.getCurrentUserEmail() ?: "User"
    val initialUsername = email.substringBefore("@")
    
    var username by remember { mutableStateOf(initialUsername) }
    var fullName by remember { mutableStateOf(profileViewModel.getUserFullName()) }
    var phoneNumber by remember { mutableStateOf("") }
    
    Scaffold(
        containerColor = Black,
        topBar = {
            ProfileTopBar(
                title = "Edit Profile",
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture Area
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(NetflixRed)
                    .clickable { /* Launch photo picker */ },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.firstOrNull()?.uppercase() ?: "U",
                    color = White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change Photo",
                        tint = White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = email,
                color = LightGray,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Form Fields
            ProfileTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                keyboardType = KeyboardType.Text
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ProfileTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Full Name",
                keyboardType = KeyboardType.Text
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ProfileTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = "Phone Number (Optional)",
                keyboardType = KeyboardType.Phone
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Save Button
            Button(
                onClick = onSaveComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NetflixRed,
                    contentColor = White
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = LightGray) },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = DarkGray.copy(alpha = 0.7f),
            focusedContainerColor = MediumGray.copy(alpha = 0.7f),
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = NetflixRed,
            unfocusedTextColor = White,
            focusedTextColor = White,
            unfocusedLabelColor = LightGray,
            focusedLabelColor = White
        ),
        shape = RoundedCornerShape(4.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        singleLine = true
    )
}