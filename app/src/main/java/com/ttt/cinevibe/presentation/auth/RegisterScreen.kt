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
import androidx.compose.ui.res.stringResource
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
    val firebaseAuthState by viewModel.firebaseAuthState.collectAsState()
    val backendRegState by viewModel.backendRegState.collectAsState()
    val backendSyncAttempted by viewModel.backendSyncAttempted.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Removed hasNavigated state

    // Handle navigation and errors based on the final registerState
    LaunchedEffect(key1 = registerState) {
        android.util.Log.d("RegistrationDebug", "RegisterScreen LaunchedEffect triggered. State: $registerState")
        when (val state = registerState) { // Use 'state' for easier access
            is AuthState.Success -> {
                android.util.Log.d("RegistrationDebug", "State is Success. Calling onRegisterSuccess...")
                // Navigate only when the overall registration is successful
                android.util.Log.d("RegisterScreen", "RegisterState is Success, navigating.")
                // Optional: Add a small delay for smoother transition if needed
                // kotlinx.coroutines.delay(100)
                onRegisterSuccess()
                // Reset state after navigation to prevent re-triggering on recomposition
                // Consider if resetting is needed or handled elsewhere (e.g., in ViewModel on screen exit)
                 viewModel.resetAuthStates() // Resetting here might be appropriate
            }
            is AuthState.Error -> {
                android.util.Log.e("RegistrationDebug", "State is Error: ${state.message}. Showing Snackbar...")
                // Show error message
                android.util.Log.e("RegisterScreen", "RegisterState is Error: ${state.message}")
                snackbarHostState.showSnackbar(message = state.message)
                // Reset state after showing error
                viewModel.resetAuthStates()
            }
            is AuthState.Loading -> {
                android.util.Log.d("RegistrationDebug", "State is Loading.")
                android.util.Log.d("RegisterScreen", "RegisterState is Loading.")
                // Loading indicator is handled below
            }
            is AuthState.Idle -> {
                 android.util.Log.d("RegisterScreen", "RegisterState is Idle.")
                // Initial state or after reset
            }
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
                    text = stringResource(R.string.app_name),
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    color = NetflixRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Register Form Section
                Text(
                    text = stringResource(R.string.sign_up),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = White,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Text(
                    text = stringResource(R.string.register),
                    fontSize = 14.sp,
                    color = LightGray,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Username Field
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text(stringResource(R.string.username), color = LightGray) },
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
                    placeholder = { Text(stringResource(R.string.email), color = LightGray) },
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
                    placeholder = { Text(stringResource(R.string.password), color = LightGray) },
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
                        placeholder = { Text(stringResource(R.string.confirm_password), color = LightGray) },
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
                            stringResource(R.string.error_passwords_dont_match), 
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
                         android.util.Log.d("RegistrationDebug", "Register Button Clicked.")
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
                        stringResource(R.string.sign_up),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Login Section
                Text(
                    stringResource(R.string.already_have_account),
                    color = LightGray,
                    fontSize = 14.sp
                )
                
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.sign_in),
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            
            // Loading Indicator - Show whenever the overall registration state is Loading
             android.util.Log.d("RegistrationDebug", "Checking loading indicator visibility. registerState is Loading: ${registerState is AuthState.Loading}")
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