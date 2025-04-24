package com.ttt.cinevibe.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttt.cinevibe.R
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.White

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        // Background gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.8f),
                            Color.Black
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Logo
            Text(
                text = "CINEVIBE",
                color = NetflixRed,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(80.dp))
            
            // Login title
            Text(
                text = "Sign In",
                color = White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Email field
            TextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = { Text("Email or phone number", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = DarkGray,
                    focusedContainerColor = DarkGray,
                    unfocusedTextColor = White,
                    focusedTextColor = White,
                    cursorColor = White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(4.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password field
            TextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = { Text("Password", color = Color.Gray) },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = DarkGray,
                    focusedContainerColor = DarkGray,
                    unfocusedTextColor = White,
                    focusedTextColor = White,
                    cursorColor = White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(4.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sign in button
            Button(
                onClick = { onLoginClick(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NetflixRed
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Need help?
            TextButton(
                onClick = { /* TODO: Implement help functionality */ },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text(
                    text = "Need help?",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Sign up now
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "New to CineVibe?",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                TextButton(onClick = onRegisterClick) {
                    Text(
                        text = "Sign up now",
                        color = White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}