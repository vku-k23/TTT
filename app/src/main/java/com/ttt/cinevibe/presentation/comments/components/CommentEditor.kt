package com.ttt.cinevibe.presentation.comments.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CommentEditor(
    initialContent: String = "",
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit = {},
    isEditing: Boolean = false,
    placeholder: String = "Add a comment...",
    maxLength: Int = 500,  // Added character limit with 500 as default
    modifier: Modifier = Modifier
) {
    var commentText by remember { mutableStateOf(initialContent) }
    val focusRequester = remember { FocusRequester() }
    val characterCount = commentText.length
    val isAtLimit = characterCount >= maxLength
    
    LaunchedEffect(Unit) {
        delay(100) // Small delay to ensure the UI is ready
        focusRequester.requestFocus()
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        if (isEditing) {
            Text(
                text = "Edit Comment",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {            TextField(
                value = commentText,
                onValueChange = { 
                    if (it.length <= maxLength) {
                        commentText = it 
                    }
                },
                placeholder = {
                    Text(
                        text = placeholder,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                textStyle = TextStyle(fontSize = 14.sp),
                maxLines = 5,
                supportingText = {
                    if (characterCount > 0 || isAtLimit) {
                        Text(
                            text = "$characterCount/$maxLength",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAtLimit) MaterialTheme.colorScheme.error 
                                  else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                },
                isError = isAtLimit
            )
            
            IconButton(
                onClick = {
                    val trimmedText = commentText.trim()
                    if (trimmedText.isNotEmpty()) {
                        onSubmit(trimmedText)
                        commentText = ""
                    }
                },
                enabled = commentText.trim().isNotEmpty()
            ) {                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (commentText.trim().isNotEmpty()) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
        
        if (isEditing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        val trimmedText = commentText.trim()
                        if (trimmedText.isNotEmpty()) {
                            onSubmit(trimmedText)
                        }
                    },
                    enabled = commentText.trim().isNotEmpty()
                ) {
                    Text("Save")
                }
            }
        }
    }
}
