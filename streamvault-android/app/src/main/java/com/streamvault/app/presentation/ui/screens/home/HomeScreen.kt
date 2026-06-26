package com.streamvault.app.presentation.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamvault.app.domain.model.Movie
import com.streamvault.app.presentation.ui.components.*
import com.streamvault.app.presentation.ui.theme.*
import com.streamvault.app.presentation.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onMovieClick: (Int, Boolean) -> Unit,
    onPlayClick: (Movie) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading && state.trendingMovies.isEmpty()) {
        Box(Modifier.fillMaxSize().background(AmoledBlack), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentRed)
        }
        return
    }

    if (state.error != null && state.trendingMovies.isEmpty()) {
        Box(Modifier.fillMaxSize().background(AmoledBlack), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Something went wrong", color = TextSecondary)
                Button(
                    onClick = viewModel::retry,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) { Text("Retry") }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AmoledBlack),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        if (state.featured.isNotEmpty()) {
            item {
                val pagerState = rememberPagerState { state.featured.size }
                HorizontalPager(state = pagerState) { page ->
                    FeaturedBanner(
                        movie = state.featured[page],
                        onPlay = { onPlayClick(state.featured[page]) },
                        onMoreInfo = { onMovieClick(state.featured[page].id, false) }
                    )
                }
            }
        }

        if (state.trendingMovies.isNotEmpty()) {
            item { SectionHeader("Trending Now") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.trendingMovies) { movie ->
                        MovieCard(movie = movie, onClick = { onMovieClick(movie.id, false) })
                    }
                }
            }
        }

        if (state.popularMovies.isNotEmpty()) {
            item { SectionHeader("Popular Movies") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.popularMovies) { movie ->
                        MovieCard(movie = movie, onClick = { onMovieClick(movie.id, false) })
                    }
                }
            }
        }

        if (state.popularTvShows.isNotEmpty()) {
            item { SectionHeader("Popular Series") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.popularTvShows) { show ->
                        TvCard(show = show, onClick = { onMovieClick(show.id, true) })
                    }
                }
            }
        }

        if (state.topRatedMovies.isNotEmpty()) {
            item { SectionHeader("Top Rated") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.topRatedMovies) { movie ->
                        MovieCard(movie = movie, onClick = { onMovieClick(movie.id, false) })
                    }
                }
            }
        }

        if (state.nowPlaying.isNotEmpty()) {
            item { SectionHeader("Now Playing") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.nowPlaying) { movie ->
                        MovieCard(movie = movie, onClick = { onMovieClick(movie.id, false) })
                    }
                }
            }
        }

        if (state.upcoming.isNotEmpty()) {
            item { SectionHeader("Coming Soon") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.upcoming) { movie ->
                        MovieCard(movie = movie, onClick = { onMovieClick(movie.id, false) })
                    }
                }
            }
        }
    }
}
