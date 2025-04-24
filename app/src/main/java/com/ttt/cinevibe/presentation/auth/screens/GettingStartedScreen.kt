package com.ttt.cinevibe.presentation.auth.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttt.cinevibe.R // Assuming R is correctly configured for resources
import com.ttt.cinevibe.ui.theme.NetflixRed // Assuming you have a custom color defined

@Composable
fun GettingStartedScreen(
    onGetStartedClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Placeholder background
    ) {
        // TODO: Add background image with movie posters as seen in the design
        // Example:
        // Image(
        //     painter = painterResource(id = R.drawable.background_movies), // Replace with your background resource
        //     contentDescription = null,
        //     contentScale = ContentScale.Crop,
        //     modifier = Modifier.fillMaxSize()
        // )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "Dive into the World of endless entertainment",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Button(
                onClick = onGetStartedClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NetflixRed) // Use your Netflix Red color
            ) {
                Text("Get Started", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                // Assuming a border or outline for the Sign In button in the design
                // border = BorderStroke(1.dp, Color.White)
            ) {
                Text("Sign In", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GettingStartedScreenPreview() {
    GettingStartedScreen(onGetStartedClick = {}, onSignInClick = {})
}