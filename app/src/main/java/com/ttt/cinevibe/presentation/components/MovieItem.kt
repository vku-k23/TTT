package com.ttt.cinevibe.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.ui.theme.Blue
import com.ttt.cinevibe.utils.Constants

@Composable
fun MovieItem(
    movie: Movie,
    onMovieClick: (Movie) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onMovieClick(movie) },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            // Movie poster
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(
                        if (movie.posterPath != null)
                            Constants.IMAGE_BASE_URL + Constants.IMAGE_SIZE_W500 + movie.posterPath
                        else
                            "https://via.placeholder.com/500x750?text=No+Image"
                    )
                    .build(),
                contentDescription = "Movie poster for ${movie.title}",
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
                contentScale = ContentScale.Crop
            )

            // Movie details
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Movie title
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Movie description
                Text(
                    text = movie.overview,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Release date and rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (movie.releaseDate?.isNotEmpty() == true) {
                        Text(
                            text = movie.releaseDate.split("-")[0], // Just get the year
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Blue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%.1f", movie.voteAverage),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}