package com.streamvault.app.presentation.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.streamvault.app.domain.model.Favorite
import com.streamvault.app.presentation.ui.components.RatingBadge
import com.streamvault.app.presentation.ui.theme.*
import com.streamvault.app.util.toTmdbImageUrl

@Composable
fun FavoritesContent(
    favorites: List<Favorite>,
    onMovieClick: (Int, Boolean) -> Unit,
    onRemove: (Int) -> Unit
) {
    if (favorites.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🤍", fontSize = 48.sp)
                Text("No favorites yet", color = TextSecondary)
                Text("Tap ♥ on any title to add it here", color = TextTertiary, fontSize = 12.sp)
            }
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(favorites) { fav ->
            Column(
                modifier = Modifier
                    .width(100.dp)
                    .clickable { onMovieClick(fav.movieId, fav.mediaType == "tv") },
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BlackCard)
                ) {
                    AsyncImage(
                        model = fav.posterPath?.toTmdbImageUrl("w342"),
                        contentDescription = fav.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (fav.rating > 0) {
                        RatingBadge(
                            rating = fav.rating,
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                        )
                    }
                    IconButton(
                        onClick = { onRemove(fav.movieId) },
                        modifier = Modifier.align(Alignment.BottomEnd).size(32.dp)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = "Remove", tint = AccentRed, modifier = Modifier.size(16.dp))
                    }
                }
                Text(
                    text = fav.title,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
