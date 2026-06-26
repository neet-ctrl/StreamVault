package com.streamvault.app.presentation.ui.screens.details

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.streamvault.app.presentation.ui.components.*
import com.streamvault.app.presentation.ui.theme.*
import com.streamvault.app.presentation.viewmodel.DetailsViewModel
import com.streamvault.app.util.toRuntimeString
import com.streamvault.app.util.toTmdbImageUrl

@Composable
fun MovieDetailsScreen(
    movieId: Int,
    isTv: Boolean,
    onBack: () -> Unit,
    onStreamClick: (String) -> Unit,
    onSimilarClick: (Int, Boolean) -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(movieId) {
        if (isTv) viewModel.loadTvDetails(movieId)
        else viewModel.loadMovieDetails(movieId)
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize().background(AmoledBlack)) {
            ShimmerDetailScreen()
        }
        return
    }

    if (state.error != null) {
        Box(Modifier.fillMaxSize().background(AmoledBlack), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("💔", fontSize = 48.sp)
                Text("Failed to load", color = TextSecondary)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentRed)
                        .clickable { onBack() }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text("Go Back", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val title = state.movieDetails?.movie?.title ?: state.tvDetails?.tvShow?.name ?: ""
    val backdrop = state.movieDetails?.movie?.backdropPath ?: state.tvDetails?.tvShow?.backdropPath
    val poster = state.movieDetails?.movie?.posterPath ?: state.tvDetails?.tvShow?.posterPath
    val overview = state.movieDetails?.movie?.overview ?: state.tvDetails?.tvShow?.overview ?: ""
    val rating = state.movieDetails?.movie?.voteAverage ?: state.tvDetails?.tvShow?.voteAverage ?: 0.0
    val genres = state.movieDetails?.movie?.genres ?: state.tvDetails?.tvShow?.genres ?: emptyList()
    val runtime = state.movieDetails?.movie?.runtime
    val releaseDate = state.movieDetails?.movie?.releaseDate ?: state.tvDetails?.tvShow?.firstAirDate ?: ""
    val tagline = state.movieDetails?.movie?.tagline
    val imdbId = state.movieDetails?.movie?.imdbId ?: state.tvDetails?.tvShow?.imdbId
    val cast = state.movieDetails?.cast ?: state.tvDetails?.cast ?: emptyList()
    val similar = state.movieDetails?.similar ?: emptyList()

    Box(modifier = Modifier.fillMaxSize().background(AmoledBlack)) {
        LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {

            // Backdrop hero
            item {
                Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                    AsyncImage(
                        model = backdrop?.toTmdbImageUrl("w1280"),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Cinematic gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color(0x33000000),
                                        0.5f to Color(0x66000000),
                                        1.0f to AmoledBlack
                                    )
                                )
                            )
                    )
                    // Back & favorite buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0x88000000))
                                .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowBack, null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0x88000000))
                                .border(
                                    0.5.dp,
                                    if (state.isFavorite) AccentRed.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f),
                                    CircleShape
                                )
                                .clickable { viewModel.toggleFavorite() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                null,
                                tint = if (state.isFavorite) AccentRed else TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Info row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .offset(y = (-20).dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Poster with shadow
                    Box(
                        modifier = Modifier
                            .width(115.dp)
                            .height(172.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BlackCard)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    ) {
                        AsyncImage(
                            model = poster?.toTmdbImageUrl("w342"),
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            title,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 28.sp
                        )
                        if (!tagline.isNullOrBlank()) {
                            Text(
                                "\"$tagline\"",
                                color = AccentRed.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RatingBadge(rating)
                            if (runtime != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BlackElevated)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(runtime.toRuntimeString(), color = TextTertiary, fontSize = 12.sp)
                                }
                            }
                        }
                        if (releaseDate.length >= 4) {
                            Text(releaseDate.take(4), color = TextTertiary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        if (genres.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                genres.take(2).forEach { genre ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(AccentRed.copy(alpha = 0.1f))
                                            .border(0.5.dp, AccentRed.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(genre, color = AccentRed.copy(alpha = 0.9f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Play & Add buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (imdbId != null)
                                    Brush.linearGradient(listOf(AccentRed, Color(0xFFB71C1C)))
                                else
                                    Brush.linearGradient(listOf(BlackElevated, BlackElevated))
                            )
                            .clickable(enabled = imdbId != null) {
                                imdbId?.let { onStreamClick(it) }
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                            Text("Play Now", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(BlackCard)
                            .border(1.dp, BlackBorder, RoundedCornerShape(14.dp))
                            .clickable { viewModel.toggleFavorite() }
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (state.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            null,
                            tint = if (state.isFavorite) AccentRed else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Overview
            if (overview.isNotBlank()) {
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BlackCard)
                            .border(0.5.dp, BlackBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            "Synopsis",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(overview, color = TextSecondary, fontSize = 14.sp, lineHeight = 22.sp)
                    }
                }
            }

            // Cast
            if (cast.isNotEmpty()) {
                item { SectionHeader("Cast") }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(cast) { member ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(72.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(66.dp)
                                        .clip(CircleShape)
                                        .background(BlackCard)
                                        .border(1.dp, BlackBorder, CircleShape)
                                ) {
                                    AsyncImage(
                                        model = member.profilePath?.toTmdbImageUrl("w185"),
                                        contentDescription = member.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    member.name,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(72.dp)
                                )
                                Text(
                                    member.character,
                                    color = TextTertiary,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(72.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Similar
            if (similar.isNotEmpty()) {
                item { SectionHeader("More Like This") }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(similar) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onSimilarClick(movie.id, isTv) }
                            )
                        }
                    }
                }
            }
        }
    }
}
