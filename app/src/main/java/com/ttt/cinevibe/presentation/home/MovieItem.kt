package com.ttt.cinevibe.presentation.home


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ttt.cinevibe.domain.model.Movie

@Composable
fun MovieItem(
    movie: Movie,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick?.invoke() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = movie.title)
            Text(text = "Rating: ${movie.voteAverage}")
        }
    }
}
