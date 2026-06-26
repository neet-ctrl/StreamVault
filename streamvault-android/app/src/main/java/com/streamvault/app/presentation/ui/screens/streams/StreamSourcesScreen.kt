package com.streamvault.app.presentation.ui.screens.streams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamvault.app.domain.model.Stream
import com.streamvault.app.presentation.ui.components.GlassmorphicCard
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Column {
                Text("Stream Sources", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (state.isLoading) "Loading from ${state.loadingAddons.size} addons…"
                    else "${state.streams.size} streams found",
                    color = TextTertiary,
                    fontSize = 12.sp
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(qualityFilters) { filter ->
                val selected = (filter == "All" && state.selectedQuality == null) ||
                    filter == state.selectedQuality
                FilterChip(
                    selected = selected,
                    onClick = {
                        viewModel.filterByQuality(if (filter == "All") null else filter)
                    },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentRed,
                        selectedLabelColor = TextPrimary,
                        containerColor = BlackCard,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        if (state.isLoading && state.streams.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = AccentRed)
                    Text("Fetching streams from addons…", color = TextSecondary)
                }
            }
        } else {
            val displayStreams = if (state.selectedQuality != null) {
                state.streams.filter { it.quality == state.selectedQuality }
            } else {
                state.streams
            }

            if (displayStreams.isEmpty() && !state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("😔", fontSize = 48.sp)
                        Text("No streams available", color = TextSecondary)
                        Text("Try a different quality filter", color = TextTertiary, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (state.isLoading) {
                        item {
                            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = AccentCyan
                                    )
                                    Text("Loading more streams…", color = TextTertiary, fontSize = 12.sp)
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
                }
            }
        }
    }
}
