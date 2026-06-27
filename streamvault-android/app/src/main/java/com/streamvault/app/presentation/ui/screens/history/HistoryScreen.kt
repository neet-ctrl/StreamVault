package com.streamvault.app.presentation.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
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
import com.streamvault.app.domain.model.WatchHistory
import com.streamvault.app.presentation.ui.theme.*
import com.streamvault.app.util.toProgressString

import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryContent(
    history: List<WatchHistory>,
    onMovieClick: (Int, Boolean) -> Unit,
    onRemove: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    if (history.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Text("🕐", fontSize = 64.sp)
                Text(
                    "No watch history",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Movies and series you watch\nwill appear here",
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
        // Header row with count + clear button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${history.size} items", color = TextTertiary, fontSize = 12.sp)
            var showConfirm by remember { mutableStateOf(false) }
            TextButton(onClick = { showConfirm = true }) {
                Text("Clear All", color = ErrorRed.copy(alpha = 0.8f), fontSize = 12.sp)
            }
            if (showConfirm) {
                AlertDialog(
                    onDismissRequest = { showConfirm = false },
                    containerColor = BlackCard,
                    shape = RoundedCornerShape(20.dp),
                    title = { Text("Clear History?", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    text = { Text("This will remove all ${history.size} items from your watch history.", color = TextSecondary) },
                    confirmButton = {
                        TextButton(onClick = { onClearAll(); showConfirm = false }) {
                            Text("Clear All", color = ErrorRed, fontWeight = FontWeight.Bold)
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

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(history, key = { it.id }) { item ->
                HistoryItemCard(
                    item = item,
                    onClick = { onMovieClick(item.movieId, item.mediaType == "tv") },
                    onRemove = { onRemove(item.movieId) }
                )
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun HistoryItemCard(
    item: WatchHistory,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val progress = item.progressPercent.coerceIn(0f, 1f)
    val dateStr = remember(item.watchedAt) {
        SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(item.watchedAt))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BlackCard)
            .border(0.5.dp, BlackBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Poster thumbnail
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(130.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                    .background(BlackElevated)
            ) {
                AsyncImage(
                    model = item.posterPath,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Play overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x66000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        null,
                        tint = TextPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    item.title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // Episode info
                if (item.season != null && item.episode != null) {
                    Text(
                        "S${item.season} E${item.episode}",
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                // Date
                Text(dateStr, color = TextTertiary, fontSize = 11.sp)
                // Progress info
                if (item.durationMs > 0) {
                    Text(
                        "${item.progressMs.toProgressString()} / ${item.durationMs.toProgressString()}",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.weight(1f))
                // Progress bar
                if (progress > 0f) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = AccentRed,
                            trackColor = BlackElevated,
                        )
                        if (progress >= 0.9f) {
                            Text("Watched", color = Success, fontSize = 10.sp)
                        }
                    }
                }
            }

            // Delete button
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BlackElevated)
                    .border(0.5.dp, BlackBorder, RoundedCornerShape(8.dp))
                    .clickable(onClick = onRemove)
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Delete,
                    null,
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
