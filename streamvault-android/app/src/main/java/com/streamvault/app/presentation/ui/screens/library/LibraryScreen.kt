package com.streamvault.app.presentation.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val tabs = listOf("Favorites", "History")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .statusBarsPadding()
    ) {
        Text(
            "My Library",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BlackSurface,
            contentColor = AccentRed,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AccentRed
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            color = if (selectedTab == index) AccentRed else TextTertiary
                        )
                    }
                )
            }
        }

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
