package com.streamvault.app.presentation.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.streamvault.app.domain.model.MediaType
import com.streamvault.app.presentation.ui.components.RatingBadge
import com.streamvault.app.presentation.ui.components.SectionHeader
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
        Box(Modifier.fillMaxSize().background(AmoledBlack), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentRed)
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
    val imdbId = state.movieDetails?.movie?.imdbId ?: state.tvDetails?.tvShow?.imdbId
    val cast = state.movieDetails?.cast ?: state.tvDetails?.cast ?: emptyList()
    val similar = state.movieDetails?.similar ?: emptyList()

    Box(modifier = Modifier.fillMaxSize().background(AmoledBlack)) {
        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(380.dp)) {
                    AsyncImage(
                        model = backdrop?.toTmdbImageUrl("w1280"),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, AmoledBlack),
                                startY = 150f
                            )
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x88000000))
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        IconButton(
                            onClick = viewModel::toggleFavorite,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x88000000))
                        ) {
                            Icon(
                                if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (state.isFavorite) AccentRed else TextPrimary
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AsyncImage(
                        model = poster?.toTmdbImageUrl("w342"),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(110.dp)
                            .height(165.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BlackCard)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(title, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RatingBadge(rating)
                            if (runtime != null) {
                                Text(runtime.toRuntimeString(), color = TextTertiary, fontSize = 13.sp)
                            }
                        }
                        if (releaseDate.length >= 4) {
                            Text(releaseDate.take(4), color = TextTertiary, fontSize = 13.sp)
                        }
                        if (genres.isNotEmpty()) {
                            Text(
                                genres.take(3).joinToString(" • "),
                                color = TextTertiary,
                                fontSize = 12.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    imdbId?.let { onStreamClick(it) }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                enabled = imdbId != null
                            ) { Text("▶  Play") }
                        }
                    }
                }
            }

            if (overview.isNotBlank()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Overview", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(overview, color = TextSecondary, fontSize = 14.sp, lineHeight = 22.sp)
                    }
                }
            }

            if (cast.isNotEmpty()) {
                item { SectionHeader("Cast") }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(cast) { member ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(80.dp)
                            ) {
                                AsyncImage(
                                    model = member.profilePath?.toTmdbImageUrl("w185"),
                                    contentDescription = member.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .background(BlackCard)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    member.name,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    member.character,
                                    color = TextTertiary,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            if (similar.isNotEmpty()) {
                item { SectionHeader("Similar") }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(similar) { movie ->
                            com.streamvault.app.presentation.ui.components.MovieCard(
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
