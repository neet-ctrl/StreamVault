package com.streamvault.app.presentation.ui.screens.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.streamvault.app.domain.model.Download
import com.streamvault.app.domain.model.DownloadStatus
import com.streamvault.app.presentation.ui.components.QualityBadge
import com.streamvault.app.presentation.ui.theme.*
import com.streamvault.app.presentation.viewmodel.DownloadsViewModel
import com.streamvault.app.util.toFormattedSize
import com.streamvault.app.util.toTmdbImageUrl

@Composable
fun DownloadsScreen(viewModel: DownloadsViewModel = hiltViewModel()) {
    val downloads by viewModel.downloads.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        // Premium header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(colors = listOf(BlackSurface, AmoledBlack)))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Downloads", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
                    Text("${downloads.size} items", color = TextTertiary, fontSize = 13.sp)
                }
                if (downloads.isNotEmpty()) {
                    val activeCount = downloads.count { it.status == DownloadStatus.DOWNLOADING }
                    if (activeCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Success.copy(alpha = 0.15f))
                                .border(0.5.dp, Success.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(Modifier.size(12.dp), color = Success, strokeWidth = 1.5.dp)
                                Text("$activeCount active", color = Success, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        if (downloads.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("📥", fontSize = 64.sp)
                    Text("No downloads yet", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Downloaded content will appear here", color = TextTertiary, fontSize = 14.sp)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BlackCard)
                            .border(0.5.dp, BlackBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text("Browse content to download", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(downloads) { download ->
                    DownloadItem(download = download, onDelete = { viewModel.deleteDownload(download.id) })
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun DownloadItem(download: Download, onDelete: () -> Unit) {
    val progressFraction = if (download.totalSize > 0)
        (download.downloadedSize.toFloat() / download.totalSize.toFloat()).coerceIn(0f, 1f)
    else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BlackCard)
            .border(
                width = 0.5.dp,
                color = when (download.status) {
                    DownloadStatus.DOWNLOADING -> AccentCyan.copy(alpha = 0.3f)
                    DownloadStatus.COMPLETED -> Success.copy(alpha = 0.3f)
                    DownloadStatus.FAILED -> ErrorRed.copy(alpha = 0.3f)
                    else -> BlackBorder
                },
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        // Progress background fill
        if (download.status == DownloadStatus.DOWNLOADING && progressFraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFraction)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(AccentRed.copy(alpha = 0.08f), Color.Transparent)
                        )
                    )
            )
        }

        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(88.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BlackElevated)
                    .border(0.5.dp, BlackBorder, RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = download.posterPath?.toTmdbImageUrl("w185"),
                    contentDescription = download.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(download.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    QualityBadge(download.quality)
                    Text(download.totalSize.toFormattedSize(), color = TextTertiary, fontSize = 11.sp)
                }

                when (download.status) {
                    DownloadStatus.DOWNLOADING -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = AccentRed,
                                trackColor = BlackElevated
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${(progressFraction * 100).toInt()}%", color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${download.downloadedSize.toFormattedSize()} / ${download.totalSize.toFormattedSize()}", color = TextTertiary, fontSize = 11.sp)
                            }
                        }
                    }
                    DownloadStatus.COMPLETED -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(Success))
                            Text("Completed", color = Success, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    DownloadStatus.PAUSED -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(Warning))
                            Text("Paused · ${(progressFraction * 100).toInt()}%", color = Warning, fontSize = 12.sp)
                        }
                    }
                    DownloadStatus.FAILED -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(ErrorRed))
                            Text("Failed", color = ErrorRed, fontSize = 12.sp)
                        }
                    }
                    DownloadStatus.QUEUED -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(AccentCyan))
                            Text("Queued", color = AccentCyan, fontSize = 12.sp)
                        }
                    }
                    else -> {}
                }
            }

            // Action button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ErrorRed.copy(alpha = 0.1f))
                    .border(0.5.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, null, tint = ErrorRed, modifier = Modifier.size(17.dp))
            }
        }
    }
}
