package com.ttt.cinevibe.presentation.auth.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ttt.cinevibe.R // Assuming R is correctly configured for resources
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onLoadingComplete: () -> Unit) {
        LaunchedEffect(key1 = true) {
                // Simulate loading time
                kotlinx.coroutines.delay(2000) // 2 second delay
                onLoadingComplete()
        }

        Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center
        ) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                ) {
                        // Replace with your actual Netflix logo resource
                        Image(
                                painter =
                                        painterResource(
                                                id = R.drawable.netflix_logo
                                        ), // Using the correct drawable resource ID
                                contentDescription = "Netflix Logo",
                                modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
        }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
        SplashScreen(onLoadingComplete = {})
}
