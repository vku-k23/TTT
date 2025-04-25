package com.ttt.cinevibe.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ttt.cinevibe.R
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.DarkGray
import com.ttt.cinevibe.ui.theme.White
import com.ttt.cinevibe.ui.theme.LightGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val registerState by viewModel.registerState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Track if we've already performed the navigation
    val hasNavigated = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = registerState) {
        when (registerState) {
            is AuthState.Success -> {
                // Only navigate if we haven't already
                if (!hasNavigated.value) {
                    hasNavigated.value = true
                    
                    // Reset state first
                    viewModel.resetAuthStates()
                    
                    // Navigate using a slight delay to ensure state resets properly
                    kotlinx.coroutines.delay(200)
                    onRegisterSuccess()
                }
            }
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (registerState as AuthState.Error).message
                )
                viewModel.resetAuthStates()
                hasNavigated.value = false
            }
            is AuthState.Idle -> {
                // Reset navigation flag when state is idle
                hasNavigated.value = false
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Image with Gradient Overlay
        Image(
            painter = painterResource(id = R.drawable.background_movie),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f),
                                Color.Black.copy(alpha = 0.9f),
                                Color.Black
                            ),
                            startY = 0f,
                            endY = size.height
                        ),
                        blendMode = BlendMode.SrcAtop
                    )
                }
        )
        
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo and Header Section
                Text(
                    text = "CINEVIBE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    color = NetflixRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Register Form Section
                Text(
                    text = "Sign Up",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = White,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Text(
                    text = "Create your CineVibe account",
                    fontSize = 14.sp,
                    color = LightGray,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Username Field
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text("Username", color = LightGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = DarkGray.copy(alpha = 0.7f),
                        focusedContainerColor = DarkGray.copy(alpha = 0.7f),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedTextColor = White,
                        focusedTextColor = White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Email Field
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email", color = LightGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = DarkGray.copy(alpha = 0.7f),
                        focusedContainerColor = DarkGray.copy(alpha = 0.7f),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedTextColor = White,
                        focusedTextColor = White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Password Field
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password", color = LightGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = DarkGray.copy(alpha = 0.7f),
                        focusedContainerColor = DarkGray.copy(alpha = 0.7f),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedTextColor = White,
                        focusedTextColor = White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Confirm Password Field
                val passwordsMatch = password == confirmPassword || confirmPassword.isEmpty()
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Confirm Password", color = LightGray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = DarkGray.copy(alpha = 0.7f),
                            focusedContainerColor = DarkGray.copy(alpha = 0.7f),
                            unfocusedIndicatorColor = if (passwordsMatch) Color.Transparent else NetflixRed,
                            focusedIndicatorColor = if (passwordsMatch) Color.Transparent else NetflixRed,
                            unfocusedTextColor = White,
                            focusedTextColor = White
                        ),
                        shape = RoundedCornerShape(4.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        isError = !passwordsMatch,
                        supportingText = null // Remove the supporting text from inside the TextField
                    )
                    
                    // Display error message outside of TextField to maintain consistent field height
                    if (!passwordsMatch) {
                        Text(
                            "Passwords don't match", 
                            color = NetflixRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(if (passwordsMatch) 32.dp else 16.dp))
                
                // Sign Up Button
                Button(
                    onClick = {
                        if (password == confirmPassword) {
                            viewModel.register(email, password, username)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NetflixRed,
                        contentColor = White,
                        disabledContainerColor = NetflixRed.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(4.dp),
                    enabled = email.isNotEmpty() && password.isNotEmpty() && 
                            username.isNotEmpty() && password == confirmPassword && 
                            registerState !is AuthState.Loading
                ) {
                    Text(
                        "Sign Up",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Login Section
                Text(
                    "Already have an account?",
                    color = LightGray,
                    fontSize = 14.sp
                )
                
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Sign in now",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            
            // Loading Indicator
            if (registerState is AuthState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NetflixRed)
                }
            }
        }
    }
}