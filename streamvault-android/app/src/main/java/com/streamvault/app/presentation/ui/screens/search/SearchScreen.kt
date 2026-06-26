package com.streamvault.app.presentation.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamvault.app.presentation.ui.components.MovieCard
import com.streamvault.app.presentation.ui.components.ShimmerMovieCard
import com.streamvault.app.presentation.ui.theme.*
import com.streamvault.app.presentation.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    onMovieClick: (Int, Boolean) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try { focusRequester.requestFocus() } catch (_: Exception) {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BlackSurface, AmoledBlack)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Search",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )

                // Premium search field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(BlackCard)
                        .border(
                            width = 1.dp,
                            brush = if (state.query.isNotEmpty())
                                Brush.linearGradient(listOf(AccentRed, Color(0xFFFF6B35)))
                            else
                                Brush.linearGradient(listOf(BlackBorder, BlackBorder)),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    TextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = {
                            Text("Movies, series, actors…", color = TextDisabled, fontSize = 15.sp)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = if (state.query.isNotEmpty()) AccentRed else TextTertiary,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = viewModel::clearSearch) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextTertiary)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            viewModel.search()
                            focusManager.clearFocus()
                        }),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = AccentRed,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }

        when {
            state.isLoading -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(9) { ShimmerMovieCard(width = 100, height = 150) }
                }
            }

            state.results.isEmpty() && state.hasSearched -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🔍", fontSize = 56.sp)
                        Text(
                            "No results for",
                            color = TextTertiary,
                            fontSize = 14.sp
                        )
                        Text(
                            "\"${state.query}\"",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Try a different search term",
                            color = TextTertiary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            !state.hasSearched -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("🎬", fontSize = 64.sp)
                        Text(
                            "Discover anything",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Movies, series, actors, directors…",
                            color = TextTertiary,
                            fontSize = 14.sp
                        )
                        // Popular search chips
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Action", "Comedy", "Thriller").forEach { genre ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(BlackCard)
                                        .border(0.5.dp, BlackBorder, RoundedCornerShape(20.dp))
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Text(genre, color = TextSecondary, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        "${state.results.size} results",
                        color = TextTertiary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.results) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie.id, movie.mediaType.name == "TV") },
                                width = 100.dp,
                                height = 150.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
