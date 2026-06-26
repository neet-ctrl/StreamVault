package com.streamvault.app.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.streamvault.app.presentation.ui.theme.BlackCard
import com.streamvault.app.presentation.ui.theme.BlackElevated

@Composable
fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        Color(0xFF1A1A1A),
        Color(0xFF2A2A2A),
        Color(0xFF1A1A1A)
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation - 400f, 0f),
        end = Offset(translateAnimation, 0f)
    )
}

@Composable
fun ShimmerMovieCard(
    modifier: Modifier = Modifier,
    width: Int = 130,
    height: Int = 195
) {
    val brush = shimmerBrush()
    Column(
        modifier = modifier.width(width.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(width.dp)
                .height(height.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(brush)
        )
        Box(
            modifier = Modifier
                .width((width * 0.85f).dp)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
        Box(
            modifier = Modifier
                .width((width * 0.6f).dp)
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
    }
}

@Composable
fun ShimmerFeaturedBanner() {
    val brush = shimmerBrush()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .background(brush)
    )
}

@Composable
fun ShimmerStreamCard() {
    val brush = shimmerBrush()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(brush)
    )
}

@Composable
fun ShimmerDetailScreen() {
    val brush = shimmerBrush()
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .background(brush)
        )
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(165.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(brush)
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.width(180.dp).height(22.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                Box(Modifier.width(120.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                Box(Modifier.width(90.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                Box(Modifier.width(140.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            }
        }
        Spacer(Modifier.height(8.dp))
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(12.dp)
                    .padding(start = 16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
fun ShimmerRow(count: Int = 5) {
    Row(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) { }
    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(count) {
            ShimmerMovieCard()
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.items(
    count: Int, content: @Composable () -> Unit
) {
    repeat(count) {
        item { content() }
    }
}
