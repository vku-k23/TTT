package com.ttt.cinevibe.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ttt.cinevibe.presentation.auth.AuthViewModel
import com.ttt.cinevibe.ui.theme.NetflixRed
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // Scale animation for the logo (Netflix-like effect)
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    
    // Launch animation effects
    LaunchedEffect(key1 = true) {
        // First animate the logo appearance with fade-in and scale
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = LinearEasing
            )
        )
        
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 600,
                easing = LinearEasing
            )
        )
        
        // Add a small delay to let the user see the logo
        delay(1500)
        
        // Optional: Add another subtle animation before finishing
        scale.animateTo(
            targetValue = 1.1f,
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearEasing
            )
        )
        
        // Check if user is already logged in
        if (viewModel.isUserLoggedIn()) {
            // Navigate directly to home screen if logged in
            onNavigateToHome()
        } else {
            // Navigate to auth flow if not logged in
            onSplashFinished()
        }
    }
    
    // Netflix-style splash screen with black background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // CINEVIBE Logo Text with Netflix-like styling
        Text(
            text = "CINEVIBE",
            color = NetflixRed,
            fontSize = 54.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            modifier = Modifier
                .scale(scale.value)
                .padding(16.dp)
        )
    }
}