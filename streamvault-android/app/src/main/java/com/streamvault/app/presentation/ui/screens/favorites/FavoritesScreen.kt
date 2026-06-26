package com.streamvault.app.presentation.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.streamvault.app.domain.model.Favorite
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Text("❤️", fontSize = 64.sp)
                Text(
                    "No favorites yet",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Tap ❤ on any movie or series\nto save it here",
                    color = TextTertiary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
        return
    }

    Column {
        Text(
            "${favorites.size} saved",
            color = TextTertiary,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(favorites, key = { it.id }) { fav ->
                FavoriteCard(
                    favorite = fav,
                    onClick = { onMovieClick(fav.movieId, fav.mediaType == "tv") },
                    onRemove = { onRemove(fav.movieId) }
                )
            }
            item { Spacer(Modifier.height(100.dp)) }
            item { Spacer(Modifier.height(100.dp)) }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun FavoriteCard(
    favorite: Favorite,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(BlackCard)
                .border(0.5.dp, BlackBorder, RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
        ) {
            AsyncImage(
                model = favorite.posterPath?.toTmdbImageUrl("w342"),
                contentDescription = favorite.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Bottom gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000)))
                    )
            )
            // Media type pill
            if (favorite.mediaType == "tv") {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Info.copy(alpha = 0.85f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text("TV", color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            // Rating badge
            if (favorite.rating > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xCC000000))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⭐", fontSize = 8.sp)
                        Text(
                            "%.1f".format(favorite.rating),
                            color = AccentGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            // Remove (unfav) button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xAA000000))
                    .border(0.5.dp, ErrorRed.copy(alpha = 0.5f), CircleShape)
                    .clickable { showConfirm = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Favorite, null, tint = ErrorRed, modifier = Modifier.size(12.dp))
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            favorite.title,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 15.sp
        )
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor = BlackCard,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Remove Favorite?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Remove \"${favorite.title}\" from favorites?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { onRemove(); showConfirm = false }) {
                    Text("Remove", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel", color = TextTertiary)
                }
            }
        )
    }
}
