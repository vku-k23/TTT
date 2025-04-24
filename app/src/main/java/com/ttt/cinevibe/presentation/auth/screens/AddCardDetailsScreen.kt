package com.ttt.cinevibe.presentation.auth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttt.cinevibe.ui.theme.NetflixRed // Assuming you have a custom color defined

@Composable
fun AddCardDetailsScreen(
    onContinueClick: (String, String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    var cardType by remember { mutableStateOf("") } // Placeholder for card type selection
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

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
            verticalArrangement = Arrangement.Top
        ) {
            // TODO: Implement back button
            // IconButton(onClick = onBackClick) {
            //     Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            // }

            Text(
                text = "Add card details",
                color = Color.White,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // TODO: Implement card type selection (e.g., DropdownMenu)
            Text(
                text = "Choose card type",
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            // Placeholder for card type selection UI
            OutlinedTextField(
                value = cardType,
                onValueChange = { cardType = it },
                label = { Text("Card Type", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                 colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NetflixRed,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = NetflixRed,
                    focusedLabelColor = NetflixRed,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.DarkGray,
                    unfocusedContainerColor = Color.DarkGray
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )


            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text("Card Number", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                 colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NetflixRed,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = NetflixRed,
                    focusedLabelColor = NetflixRed,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.DarkGray,
                    unfocusedContainerColor = Color.DarkGray
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Expiry date", color = Color.Gray) },
                    modifier = Modifier.weight(1f).padding(bottom = 24.dp),
                     colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NetflixRed,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = NetflixRed,
                        focusedLabelColor = NetflixRed,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.DarkGray,
                        unfocusedContainerColor = Color.DarkGray
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = cvv,
                    onValueChange = { cvv = it },
                    label = { Text("CVV", color = Color.Gray) },
                    modifier = Modifier.weight(1f).padding(bottom = 24.dp),
                     colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NetflixRed,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = NetflixRed,
                        focusedLabelColor = NetflixRed,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.DarkGray,
                        unfocusedContainerColor = Color.DarkGray
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Button(
                onClick = { onContinueClick(cardType, cardNumber, expiryDate, cvv) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NetflixRed)
            ) {
                Text("Continue", fontSize = 18.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddCardDetailsScreenPreview() {
    AddCardDetailsScreen(onContinueClick = { _, _, _, _ -> }, onBackClick = {})
}