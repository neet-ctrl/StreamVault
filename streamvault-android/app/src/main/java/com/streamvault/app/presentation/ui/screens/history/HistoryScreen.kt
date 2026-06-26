package com.streamvault.app.presentation.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
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
import com.streamvault.app.domain.model.WatchHistory
import com.streamvault.app.presentation.ui.theme.*
import com.streamvault.app.util.toTmdbImageUrl
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryContent(
    history: List<WatchHistory>,
    onMovieClick: (Int, Boolean) -> Unit,
    onRemove: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Clear History", color = TextPrimary) },
            text = { Text("Clear all watch history?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    onClearAll()
                    showConfirmDialog = false
                }) { Text("Clear", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = BlackCard
        )
    }

    if (history.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("📺", fontSize = 48.sp)
                Text("No watch history", color = TextSecondary)
                Text("Content you watch will appear here", color = TextTertiary, fontSize = 12.sp)
            }
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${history.size} titles", color = TextTertiary, fontSize = 12.sp)
            IconButton(onClick = { showConfirmDialog = true }) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = ErrorRed)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(history) { item ->
                HistoryItem(
                    item = item,
                    onClick = { onMovieClick(item.movieId, item.mediaType == "tv") },
                    onRemove = { onRemove(item.movieId) }
                )
            }
        }
    }
}

@Composable
fun HistoryItem(item: WatchHistory, onClick: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BlackCard)
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(65.dp)
                .height(90.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BlackElevated)
        ) {
            AsyncImage(
                model = item.posterPath?.toTmdbImageUrl("w185"),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (item.progressPercent > 0f) {
                LinearProgressIndicator(
                    progress = { item.progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = AccentRed,
                    trackColor = BlackElevated
                )
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(item.watchedAt))
            Text(dateStr, color = TextTertiary, fontSize = 11.sp)
            if (item.progressPercent > 0f) {
                Text(
                    "${(item.progressPercent * 100).toInt()}% watched",
                    color = AccentRed,
                    fontSize = 11.sp
                )
            }
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = TextTertiary, modifier = Modifier.size(16.dp))
        }
    }
}
