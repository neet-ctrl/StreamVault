package com.streamvault.app.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamvault.app.domain.model.Stream
import com.streamvault.app.presentation.ui.theme.*
import com.streamvault.app.util.toFormattedSize

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "card_scale"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.5f else 0.2f,
        animationSpec = tween(150),
        label = "border_alpha"
    )

    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E1E1E),
                        Color(0xFF141414)
                    )
                )
            )
            .border(
                width = 0.8.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = borderAlpha),
                        Color.White.copy(alpha = borderAlpha * 0.3f),
                        Color(0xFFE50914).copy(alpha = borderAlpha * 0.2f)
                    )
                ),
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = rememberRipple(color = Color.White.copy(alpha = 0.08f)),
                        onClick = onClick
                    )
                } else Modifier
            ),
        content = content
    )
}

@Composable
fun PremiumGlassCard(
    modifier: Modifier = Modifier,
    accentColor: Color = AccentRed,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pglass_scale"
    )
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1C1C1C),
                        Color(0xFF111111)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = rememberRipple(color = accentColor.copy(alpha = 0.1f)),
                        onClick = onClick
                    )
                } else Modifier
            ),
        content = content
    )
}

@Composable
fun QualityBadge(quality: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (quality) {
        "4K" -> Pair(
            Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))),
            AmoledBlack
        )
        "1080p" -> Pair(
            Brush.linearGradient(listOf(Color(0xFF00D4FF), Color(0xFF0096B7))),
            AmoledBlack
        )
        "720p" -> Pair(
            Brush.linearGradient(listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))),
            TextPrimary
        )
        "480p" -> Pair(
            Brush.linearGradient(listOf(Color(0xFFFF9800), Color(0xFFE65100))),
            TextPrimary
        )
        else -> Pair(
            Brush.linearGradient(listOf(BlackElevated, BlackElevated)),
            TextSecondary
        )
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = quality,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
fun RatingBadge(rating: Double, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xCC000000))
            .border(0.5.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text("★", fontSize = 11.sp, color = Color(0xFFFFD700))
        Text(
            text = String.format("%.1f", rating),
            color = Color(0xFFFFD700),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AddonBadge(addonId: String, modifier: Modifier = Modifier) {
    val (label, color) = when (addonId) {
        "torrentio" -> "⚡ Torrentio" to Color(0xFFFF6B35)
        "knightcrawler" -> "🕷 KnightCrawler" to Color(0xFF9C27B0)
        "mediafusion" -> "🌊 MediaFusion" to Color(0xFF2196F3)
        "comet" -> "☄️ Comet" to Color(0xFF00BCD4)
        "jackettio" -> "🔧 Jackettio" to Color(0xFF4CAF50)
        "cinemeta" -> "🎬 Cinemeta" to Color(0xFFFF9800)
        else -> "🔗 Custom" to TextTertiary
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StreamCard(
    stream: Stream,
    onPlay: () -> Unit,
    onDownload: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isTorrent = stream.isTorrent
    val seedHealth = when {
        (stream.seeds ?: 0) >= 50 -> Color(0xFF4CAF50)
        (stream.seeds ?: 0) >= 10 -> Color(0xFFFFC107)
        (stream.seeds ?: 0) > 0 -> Color(0xFFFF5722)
        else -> TextTertiary
    }

    PremiumGlassCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = if (isTorrent) Color(0xFF4CAF50) else AccentCyan,
        onClick = onPlay
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QualityBadge(stream.quality)
                    if (isTorrent) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                                .border(0.5.dp, Color(0xFF4CAF50).copy(alpha = 0.4f), RoundedCornerShape(5.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("⚙ TORRENT", color = Color(0xFF4CAF50), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(AccentCyan.copy(alpha = 0.15f))
                                .border(0.5.dp, AccentCyan.copy(alpha = 0.4f), RoundedCornerShape(5.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("▶ DIRECT", color = AccentCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (stream.seeds != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape = RoundedCornerShape(50))
                                    .background(seedHealth)
                            )
                            Text("${stream.seeds} seeds", color = seedHealth, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    if (onDownload != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentRed.copy(alpha = 0.12f))
                                .border(0.5.dp, AccentRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable(onClick = onDownload)
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text("⬇", fontSize = 14.sp, color = AccentRed)
                        }
                    }
                }
            }

            AddonBadge(stream.addonId)

            Text(
                text = stream.title,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                lineHeight = 17.sp
            )

            if (stream.size != null || stream.peers != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (stream.size != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("💾", fontSize = 11.sp)
                            Text(stream.size.toFormattedSize(), color = TextTertiary, fontSize = 11.sp)
                        }
                    }
                    if (stream.peers != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("👥", fontSize = 11.sp)
                            Text("${stream.peers} peers", color = TextTertiary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

private val CircleShape get() = RoundedCornerShape(50)
