package com.streamvault.app.presentation.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
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
        HomeShimmer()
        return
    }

    if (state.error != null && state.trendingMovies.isEmpty()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(AmoledBlack),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("😵", fontSize = 48.sp)
                Text(
                    "Connection failed",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Check your internet connection", color = TextTertiary, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(AccentRed, Color(0xFFB71C1C)))
                        )
                        .clickable { viewModel.retry() }
                        .padding(horizontal = 28.dp, vertical = 13.dp)
                ) {
                    Text("Retry", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Hero Carousel with pager dots
        if (state.featured.isNotEmpty()) {
            item {
                val pagerState = rememberPagerState { state.featured.size }
                Box {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        FeaturedBanner(
                            movie = state.featured[page],
                            onPlay = { onPlayClick(state.featured[page]) },
                            onMoreInfo = {
                                onMovieClick(
                                    state.featured[page].id,
                                    state.featured[page].mediaType.name == "TV"
                                )
                            }
                        )
                    }
                    PagerDots(
                        count = state.featured.size,
                        selected = pagerState.currentPage,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    )
                }
            }
        }

        if (state.trendingMovies.isNotEmpty()) {
            item { SectionHeader("🔥 Trending Now") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.trendingMovies) { movie ->
                        MovieCard(
                            movie = movie,
                            onClick = { onMovieClick(movie.id, movie.mediaType.name == "TV") }
                        )
                    }
                }
            }
        }

        if (state.popularMovies.isNotEmpty()) {
            item { SectionHeader("🎬 Popular Movies") }
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
            item { SectionHeader("📺 Popular Series") }
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
            item { SectionHeader("⭐ Top Rated") }
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
            item { SectionHeader("🎥 In Theatres") }
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
            item { SectionHeader("🚀 Coming Soon") }
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

@Composable
private fun HomeShimmer() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item { ShimmerFeaturedBanner() }
        item {
            Column {
                repeat(3) {
                    Spacer(Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .width(160.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush())
                    )
                    Spacer(Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(6) {
                            ShimmerMovieCard()
                        }
                    }
                }
            }
        }
    }
}
