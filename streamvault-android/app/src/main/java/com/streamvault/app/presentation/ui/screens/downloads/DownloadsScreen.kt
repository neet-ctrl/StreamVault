package com.streamvault.app.presentation.ui.screens.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.streamvault.app.domain.model.Download
import com.streamvault.app.domain.model.DownloadStatus
import com.streamvault.app.presentation.ui.components.GlassmorphicCard
import com.streamvault.app.presentation.ui.components.QualityBadge
import com.streamvault.app.presentation.ui.theme.*
import com.streamvault.app.presentation.viewmodel.DownloadsViewModel
import com.streamvault.app.util.toFormattedSize
import com.streamvault.app.util.toTmdbImageUrl

@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val downloads by viewModel.downloads.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .statusBarsPadding()
    ) {
        Text(
            "Downloads",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        if (downloads.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📥", fontSize = 48.sp)
                    Text("No downloads yet", color = TextSecondary)
                    Text("Downloaded content will appear here", color = TextTertiary, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(downloads) { download ->
                    DownloadItem(
                        download = download,
                        onDelete = { viewModel.deleteDownload(download.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadItem(download: Download, onDelete: () -> Unit) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = download.posterPath?.toTmdbImageUrl("w185"),
                contentDescription = download.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(60.dp)
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BlackCard)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(download.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QualityBadge(download.quality)
                    Text(
                        download.totalSize.toFormattedSize(),
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
                when (download.status) {
                    DownloadStatus.DOWNLOADING -> {
                        val progress = if (download.totalSize > 0)
                            download.downloadedSize.toFloat() / download.totalSize.toFloat()
                        else 0f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = AccentRed,
                            trackColor = BlackElevated
                        )
                        Text(
                            "${(progress * 100).toInt()}% • ${download.downloadedSize.toFormattedSize()} / ${download.totalSize.toFormattedSize()}",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                    DownloadStatus.COMPLETED -> {
                        Text("✅ Completed", color = Success, fontSize = 12.sp)
                    }
                    DownloadStatus.PAUSED -> {
                        Text("⏸ Paused", color = Warning, fontSize = 12.sp)
                    }
                    DownloadStatus.FAILED -> {
                        Text("❌ Failed", color = ErrorRed, fontSize = 12.sp)
                    }
                    DownloadStatus.QUEUED -> {
                        Text("⏳ Queued", color = AccentCyan, fontSize = 12.sp)
                    }
                    else -> {}
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(18.dp))
            }
        }
    }
}
