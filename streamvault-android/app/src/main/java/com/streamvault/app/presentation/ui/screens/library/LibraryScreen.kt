package com.streamvault.app.presentation.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamvault.app.presentation.ui.screens.favorites.FavoritesContent
import com.streamvault.app.presentation.ui.screens.history.HistoryContent
import com.streamvault.app.presentation.ui.theme.*
import com.streamvault.app.presentation.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    onMovieClick: (Int, Boolean) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("❤️ Favorites", "📺 History")

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
            Text(
                "My Library",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        }

        // Premium tab row (custom, not Material default)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(BlackCard)
                .border(0.5.dp, BlackBorder, RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, label ->
                val selected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected)
                                Brush.linearGradient(listOf(AccentRed, AccentRed.copy(alpha = 0.8f)))
                            else
                                Brush.linearGradient(listOf(AmoledBlack, AmoledBlack))
                        )
                        .clickable { selectedTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (selected) TextPrimary else TextTertiary,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        when (selectedTab) {
            0 -> FavoritesContent(
                favorites = state.favorites,
                onMovieClick = onMovieClick,
                onRemove = { viewModel.removeFavorite(it) }
            )
            1 -> HistoryContent(
                history = state.history,
                onMovieClick = onMovieClick,
                onRemove = { viewModel.removeFromHistory(it) },
                onClearAll = { viewModel.clearHistory() }
            )
        }
    }
}
