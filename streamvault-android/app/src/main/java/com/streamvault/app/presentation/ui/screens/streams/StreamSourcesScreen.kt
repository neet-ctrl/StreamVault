package com.streamvault.app.presentation.ui.screens.streams

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamvault.app.domain.model.Stream
import com.streamvault.app.presentation.ui.components.ShimmerStreamCard
import com.streamvault.app.presentation.ui.components.StreamCard
import com.streamvault.app.presentation.ui.theme.*
import com.streamvault.app.presentation.viewmodel.StreamSourcesViewModel

@Composable
fun StreamSourcesScreen(
    imdbId: String,
    isTv: Boolean = false,
    season: Int = 0,
    episode: Int = 0,
    onBack: () -> Unit,
    onStreamSelected: (Stream) -> Unit,
    viewModel: StreamSourcesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val qualityFilters = listOf("All", "4K", "1080p", "720p", "480p")

    LaunchedEffect(imdbId) {
        if (isTv) viewModel.loadTvStreams(imdbId, season, episode)
        else viewModel.loadMovieStreams(imdbId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        // Premium header with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(colors = listOf(BlackSurface, AmoledBlack))
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BlackCard)
                        .border(0.5.dp, BlackBorder, CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Stream Sources",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.isLoading && state.loadingAddons.isNotEmpty()) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                strokeWidth = 1.5.dp,
                                color = AccentRed
                            )
                        }
                        Text(
                            if (state.isLoading && state.streams.isEmpty())
                                "Searching ${state.loadingAddons.size} addons…"
                            else
                                "${state.streams.size} streams found",
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Quality filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(qualityFilters) { filter ->
                val selected = (filter == "All" && state.selectedQuality == null) ||
                        filter == state.selectedQuality
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selected)
                                Brush.linearGradient(listOf(AccentRed, Color(0xFFFF6B35)))
                            else
                                Brush.linearGradient(listOf(BlackCard, BlackCard))
                        )
                        .border(
                            0.5.dp,
                            if (selected) Color.Transparent else BlackBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            viewModel.filterByQuality(if (filter == "All") null else filter)
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        filter,
                        color = if (selected) TextPrimary else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        when {
            state.isLoading && state.streams.isEmpty() -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(6) {
                        ShimmerStreamCard()
                    }
                }
            }

            state.streams.isEmpty() && !state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("📡", fontSize = 56.sp)
                        Text("No streams available", color = TextSecondary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("All addons returned no results", color = TextTertiary, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(BlackCard)
                                .border(0.5.dp, BlackBorder, RoundedCornerShape(12.dp))
                                .clickable { onBack() }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text("Go Back", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            }

            else -> {
                val displayStreams = if (state.selectedQuality != null) {
                    state.streams.filter { it.quality == state.selectedQuality }
                } else {
                    state.streams
                }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Live loading indicator at top
                    if (state.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BlackCard)
                                    .border(0.5.dp, AccentCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = AccentCyan
                                    )
                                    Text(
                                        "Fetching from ${state.loadingAddons.joinToString(", ")}…",
                                        color = AccentCyan,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    items(displayStreams) { stream ->
                        StreamCard(
                            stream = stream,
                            onPlay = { onStreamSelected(stream) }
                        )
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

private fun <T> androidx.compose.foundation.lazy.LazyListScope.items(
    count: Int, content: @Composable () -> Unit
) {
    repeat(count) { item { content() } }
}
