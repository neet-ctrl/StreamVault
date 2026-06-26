package com.streamvault.app.presentation.ui.screens.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.streamvault.app.domain.model.Stream
import com.streamvault.app.domain.model.TorrentState
import com.streamvault.app.presentation.ui.theme.*
import com.streamvault.app.presentation.viewmodel.PlayerViewModel
import com.streamvault.app.presentation.viewmodel.StreamHealth
import com.streamvault.app.util.Constants
import com.streamvault.app.util.toEtaString
import com.streamvault.app.util.toProgressString
import com.streamvault.app.util.toSpeedString
import kotlinx.coroutines.delay
import kotlin.math.abs

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    stream: Stream,
    movieId: Int,
    title: String,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(stream) {
        viewModel.setStream(stream, movieId, title)
        viewModel.prepareTorrentStream(stream)
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    // Setup ExoPlayer when stream is ready
    LaunchedEffect(state.streamUrl, state.isStreamReady) {
        if (state.isStreamReady && state.streamUrl != null) {
            val mediaItem = MediaItem.fromUri(state.streamUrl!!)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            // Seek to saved position
            if (state.currentPositionMs > 0L) {
                exoPlayer.seekTo(state.currentPositionMs)
            }
        }
    }

    // Player listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                viewModel.onPlayPause(isPlaying)
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                viewModel.onBufferingChanged(playbackState == Player.STATE_BUFFERING)
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                viewModel.onError(error.message ?: "Playback error")
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            viewModel.saveToHistory()
        }
    }

    // Sync ExoPlayer state
    LaunchedEffect(state.playbackSpeed) { exoPlayer.setPlaybackSpeed(state.playbackSpeed) }
    LaunchedEffect(state.volume) { exoPlayer.volume = if (state.isMuted) 0f else state.volume }
    LaunchedEffect(state.isPlaying) {
        if (state.isPlaying != exoPlayer.isPlaying) {
            if (state.isPlaying) exoPlayer.play() else exoPlayer.pause()
        }
    }

    // Position tracking
    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(1000)
            if (exoPlayer.isPlaying) {
                viewModel.onPositionChanged(exoPlayer.currentPosition)
                viewModel.onDurationChanged(exoPlayer.duration.coerceAtLeast(0))
            }
        }
    }

    // Skip intro seek
    LaunchedEffect(state.introEndMs) {
        if (state.introEndMs > 0) exoPlayer.seekTo(state.introEndMs)
    }

    // Immersive mode
    LaunchedEffect(Unit) {
        val activity = context as? Activity ?: return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.apply {
                hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    // Gesture state
    var seekGestureOffsetMs by remember { mutableStateOf(0L) }
    var showSeekIndicator by remember { mutableStateOf(false) }
    var seekDirection by remember { mutableStateOf(0) } // -1 back, +1 forward

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { viewModel.toggleControls() },
                    onDoubleTap = { offset ->
                        val isRight = offset.x > size.width / 2
                        val seekMs = if (isRight) Constants.DOUBLE_TAP_SEEK_MS else -Constants.DOUBLE_TAP_SEEK_MS
                        exoPlayer.seekTo((exoPlayer.currentPosition + seekMs).coerceAtLeast(0))
                        seekDirection = if (isRight) 1 else -1
                        seekGestureOffsetMs = seekMs
                        showSeekIndicator = true
                    }
                )
            }
            .pointerInput(Unit) {
                // Gesture: horizontal = seek, left-vertical = brightness, right-vertical = volume
                var startOffset = Offset.Zero
                var gestureType = 0 // 0=none 1=seek 2=brightness 3=volume
                var startValue = 0f

                detectDragGestures(
                    onDragStart = { offset ->
                        startOffset = offset
                        gestureType = 0
                        startValue = when {
                            offset.x < size.width * 0.4f -> state.brightness
                            offset.x > size.width * 0.6f -> state.volume
                            else -> 0f
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val dx = change.position.x - startOffset.x
                        val dy = -(change.position.y - startOffset.y)

                        if (gestureType == 0) {
                            gestureType = if (abs(dx) > abs(dy) * 1.5f) 1
                            else if (startOffset.x < size.width * 0.4f) 2
                            else 3
                        }

                        when (gestureType) {
                            1 -> { // Seek
                                val seekMs = (dx / size.width * 120_000).toLong()
                                exoPlayer.seekTo((exoPlayer.currentPosition + seekMs).coerceAtLeast(0))
                            }
                            2 -> { // Brightness
                                val delta = dy / size.height
                                viewModel.setBrightness(startValue + delta)
                            }
                            3 -> { // Volume
                                val delta = dy / size.height
                                viewModel.setVolume(startValue + delta)
                            }
                        }
                    },
                    onDragEnd = {
                        gestureType = 0
                        showSeekIndicator = false
                    }
                )
            }
    ) {
        // ─────────── ExoPlayer Video View ───────────
        if (state.isStreamReady) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Pre-buffering screen
            PreBufferingOverlay(state = state)
        }

        // ─────────── Overlay gradients ───────────
        // Top gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(listOf(Color(0xCC000000), Color.Transparent))
                )
        )
        // Bottom gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD000000)))
                )
        )

        // ─────────── Buffering spinner ───────────
        if (state.isBuffering && state.isStreamReady) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentRed, modifier = Modifier.size(48.dp), strokeWidth = 3.dp)
            }
        }

        // ─────────── Seek indicator ───────────
        AnimatedVisibility(
            visible = showSeekIndicator,
            modifier = Modifier.align(if (seekDirection > 0) Alignment.CenterEnd else Alignment.CenterStart)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .clip(CircleShape)
                    .background(Color(0x88000000))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (seekDirection > 0) "⏩ +10s" else "⏪ -10s", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
        LaunchedEffect(showSeekIndicator) {
            if (showSeekIndicator) { delay(1200); showSeekIndicator = false }
        }

        // ─────────── Volume/Brightness indicator ───────────
        if (state.volume != 1.0f || state.brightness != 0.5f) {
            GestureIndicator(
                volumeLevel = state.volume,
                brightnessLevel = state.brightness,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // ─────────── Stream health indicator ───────────
        if (state.streamHealth != StreamHealth.GOOD) {
            StreamHealthBadge(
                health = state.streamHealth,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp)
            )
        }

        // ─────────── Controls overlay ───────────
        AnimatedVisibility(
            visible = state.isControlsVisible && !state.isPipMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            PlayerControlsOverlay(
                state = state,
                exoPlayer = exoPlayer,
                onBack = {
                    viewModel.saveToHistory()
                    onBack()
                },
                onPlayPause = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                },
                onSeekForward = {
                    exoPlayer.seekTo((exoPlayer.currentPosition + Constants.SKIP_FORWARD_MS).coerceAtMost(exoPlayer.duration))
                },
                onSeekBack = {
                    exoPlayer.seekTo((exoPlayer.currentPosition - Constants.SKIP_BACK_MS).coerceAtLeast(0))
                },
                onSeekTo = { ms -> exoPlayer.seekTo(ms) },
                onSpeedChange = viewModel::setPlaybackSpeed,
                onToggleLock = viewModel::toggleLock,
                onToggleMute = viewModel::toggleMute,
                onOpenSubtitles = viewModel::toggleSubtitlePanel,
                onOpenAudio = viewModel::toggleAudioPanel,
                onSleepTimer = viewModel::setSleepTimer,
                onPip = {
                    val activity = context as? Activity ?: return@PlayerControlsOverlay
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        activity.enterPictureInPictureMode(
                            PictureInPictureParams.Builder()
                                .setAspectRatio(Rational(16, 9))
                                .build()
                        )
                        viewModel.onPipModeChanged(true)
                    }
                },
                viewModel = viewModel
            )
        }

        // ─────────── Skip intro button ───────────
        AnimatedVisibility(
            visible = state.showSkipIntro,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 120.dp)
        ) {
            OutlinedButton(
                onClick = {
                    exoPlayer.seekTo(state.introEndMs)
                    viewModel.skipIntro()
                },
                border = BorderStroke(1.5.dp, AccentRed),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0x88000000),
                    contentColor = TextPrimary
                )
            ) {
                Text("Skip Intro ⏭", fontWeight = FontWeight.SemiBold)
            }
        }

        // ─────────── Next episode banner ───────────
        AnimatedVisibility(
            visible = state.showNextEpisode,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 80.dp)
        ) {
            NextEpisodeBanner(
                countdown = state.nextEpisodeCountdown,
                onPlay = { /* navigate to next episode */ },
                onDismiss = viewModel::cancelNextEpisode
            )
        }

        // ─────────── Subtitle panel ───────────
        AnimatedVisibility(
            visible = state.isSubtitlePanelOpen,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            SubtitlePanel(
                state = state,
                onSelectSubtitle = viewModel::selectSubtitle,
                onDisable = viewModel::disableSubtitles,
                onDelayChange = viewModel::setSubtitleDelay,
                onDismiss = viewModel::toggleSubtitlePanel
            )
        }

        // ─────────── Audio panel ───────────
        AnimatedVisibility(
            visible = state.isAudioPanelOpen,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            AudioPanel(
                state = state,
                onSelectTrack = viewModel::selectAudioTrack,
                onDismiss = viewModel::toggleAudioPanel
            )
        }

        // ─────────── Lock indicator ───────────
        if (state.isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x88000000))
                    .clickable { viewModel.toggleLock() }
                    .padding(12.dp)
            ) {
                Icon(Icons.Default.Lock, null, tint = AccentRed, modifier = Modifier.size(20.dp))
            }
        }

        // ─────────── Torrent stats overlay ───────────
        state.torrentStatus?.let { ts ->
            if (ts.state == TorrentState.DOWNLOADING || ts.state == TorrentState.PREBUFFERING) {
                TorrentStatsOverlay(
                    status = ts,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 60.dp, end = 16.dp)
                )
            }
        }

        // ─────────── Sleep timer countdown ───────────
        if (state.sleepTimerRemainingMs > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 14.dp, end = 14.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xAA000000))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    "😴 ${(state.sleepTimerRemainingMs / 60_000)}:${"%02d".format((state.sleepTimerRemainingMs % 60_000) / 1000)}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pre-buffering screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PreBufferingOverlay(state: com.streamvault.app.presentation.viewmodel.PlayerUiState) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            CircularProgressIndicator(color = AccentRed, modifier = Modifier.size(56.dp), strokeWidth = 3.dp)
            Text(
                if (state.torrentStatus?.state == TorrentState.PREBUFFERING) "Pre-buffering torrent…" else "Loading stream…",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            state.torrentStatus?.let { ts ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { ts.progress },
                        modifier = Modifier.width(200.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = AccentRed,
                        trackColor = BlackCard
                    )
                    Text(
                        "⬇ ${ts.downloadSpeedBps.toSpeedString()}  👤 ${ts.seeds}  🔗 ${ts.peers}",
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Full controls overlay
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PlayerControlsOverlay(
    state: com.streamvault.app.presentation.viewmodel.PlayerUiState,
    exoPlayer: ExoPlayer,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onToggleLock: () -> Unit,
    onToggleMute: () -> Unit,
    onOpenSubtitles: () -> Unit,
    onOpenAudio: () -> Unit,
    onSleepTimer: (Int?) -> Unit,
    onPip: () -> Unit,
    viewModel: PlayerViewModel
) {
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showSleepMenu by remember { mutableStateOf(false) }

    if (state.isLocked) return

    Box(Modifier.fillMaxSize()) {
        // ─── Top bar ───
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    state.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                state.stream?.let { s ->
                    Text(
                        "${s.quality} • ${s.provider}${if (s.isTorrent) " • 🧲" else ""}",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
            }
            // PiP button
            IconButton(onClick = onPip) {
                Icon(Icons.Default.PictureInPicture, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
            // Lock button
            IconButton(onClick = onToggleLock) {
                Icon(Icons.Default.LockOpen, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
        }

        // ─── Center playback controls ───
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rewind
            IconButton(onClick = onSeekBack, modifier = Modifier.size(52.dp)) {
                Icon(Icons.Default.Replay10, null, tint = TextPrimary, modifier = Modifier.size(36.dp))
            }
            // Play/Pause
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(AccentRed, Color(0xFFB00710)))
                    )
                    .clickable(onClick = onPlayPause),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    null,
                    tint = TextPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            // Forward
            IconButton(onClick = onSeekForward, modifier = Modifier.size(52.dp)) {
                Icon(Icons.Default.Forward10, null, tint = TextPrimary, modifier = Modifier.size(36.dp))
            }
        }

        // ─── Bottom bar ───
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Progress row
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(state.currentPositionMs.toProgressString(), color = TextSecondary, fontSize = 12.sp)
                Text(state.durationMs.toProgressString(), color = TextTertiary, fontSize = 12.sp)
            }
            // Seek slider
            if (state.durationMs > 0) {
                Slider(
                    value = state.currentPositionMs.toFloat(),
                    onValueChange = { onSeekTo(it.toLong()) },
                    valueRange = 0f..state.durationMs.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = AccentRed,
                        activeTrackColor = AccentRed,
                        inactiveTrackColor = BlackBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subtitle
                PlayerActionButton("CC", onClick = onOpenSubtitles)
                // Audio track
                PlayerActionButton("🎵", onClick = onOpenAudio)
                // Speed
                Box {
                    PlayerActionButton("${state.playbackSpeed}x", onClick = { showSpeedMenu = true })
                    DropdownMenu(
                        expanded = showSpeedMenu,
                        onDismissRequest = { showSpeedMenu = false },
                        containerColor = BlackCard
                    ) {
                        Constants.SPEED_OPTIONS.forEach { speed ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${speed}x",
                                        color = if (state.playbackSpeed == speed) AccentRed else TextPrimary,
                                        fontWeight = if (state.playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = { onSpeedChange(speed); showSpeedMenu = false }
                            )
                        }
                    }
                }
                // Mute
                PlayerActionButton(
                    if (state.isMuted) "🔇" else "🔊",
                    onClick = onToggleMute
                )
                // Sleep timer
                Box {
                    PlayerActionButton("😴", onClick = { showSleepMenu = true })
                    DropdownMenu(
                        expanded = showSleepMenu,
                        onDismissRequest = { showSleepMenu = false },
                        containerColor = BlackCard
                    ) {
                        listOf(null, 15, 30, 45, 60, 90).forEach { mins ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (mins == null) "Off" else "${mins}m",
                                        color = if (state.sleepTimerMinutes == mins) AccentRed else TextPrimary
                                    )
                                },
                                onClick = { onSleepTimer(mins); showSleepMenu = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerActionButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x55000000))
            .border(0.5.dp, BlackBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Subtitle Panel
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SubtitlePanel(
    state: com.streamvault.app.presentation.viewmodel.PlayerUiState,
    onSelectSubtitle: (Int) -> Unit,
    onDisable: () -> Unit,
    onDelayChange: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(BlackCard.copy(alpha = 0.95f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Subtitles", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
            }
        }
        // Off option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (state.activeSubtitleIndex == -1) AccentRed.copy(0.15f) else Color.Transparent)
                .border(if (state.activeSubtitleIndex == -1) 1.dp else 0.dp, AccentRed.copy(0.4f), RoundedCornerShape(10.dp))
                .clickable(onClick = onDisable)
                .padding(12.dp)
        ) {
            Text("Off", color = if (state.activeSubtitleIndex == -1) AccentRed else TextSecondary)
        }
        state.subtitles.forEachIndexed { idx, sub ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (state.activeSubtitleIndex == idx) AccentRed.copy(0.15f) else Color.Transparent)
                    .border(if (state.activeSubtitleIndex == idx) 1.dp else 0.dp, AccentRed.copy(0.4f), RoundedCornerShape(10.dp))
                    .clickable { onSelectSubtitle(idx) }
                    .padding(12.dp)
            ) {
                Column {
                    Text(sub.lang, color = if (state.activeSubtitleIndex == idx) AccentRed else TextSecondary)
                    if (sub.isExternal) Text("External", color = TextTertiary, fontSize = 10.sp)
                }
            }
        }
        // Delay control
        if (state.activeSubtitleIndex >= 0) {
            Divider(color = BlackBorder)
            Text("Delay: ${state.subtitleDelayMs}ms", color = TextTertiary, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(-500L, -100L, 100L, 500L).forEach { delta ->
                    OutlinedButton(
                        onClick = { onDelayChange(state.subtitleDelayMs + delta) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("${if (delta > 0) "+" else ""}${delta}ms", fontSize = 11.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Audio Panel
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AudioPanel(
    state: com.streamvault.app.presentation.viewmodel.PlayerUiState,
    onSelectTrack: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(BlackCard.copy(alpha = 0.95f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Audio Tracks", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
            }
        }
        if (state.audioTracks.isEmpty()) {
            Text("No additional audio tracks available", color = TextTertiary, fontSize = 13.sp)
        } else {
            state.audioTracks.forEach { track ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (state.activeAudioTrackId == track.id) AccentRed.copy(0.15f) else Color.Transparent)
                        .border(if (state.activeAudioTrackId == track.id) 1.dp else 0.dp, AccentRed.copy(0.4f), RoundedCornerShape(10.dp))
                        .clickable { onSelectTrack(track.id) }
                        .padding(12.dp)
                ) {
                    Text(
                        "${track.label} (${track.language})",
                        color = if (state.activeAudioTrackId == track.id) AccentRed else TextSecondary
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Supporting composables
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GestureIndicator(
    volumeLevel: Float,
    brightnessLevel: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x88000000))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🔆 ${(brightnessLevel * 100).toInt()}%", color = TextPrimary, fontSize = 12.sp)
        Text(if (volumeLevel <= 0f) "🔇" else "🔊 ${(volumeLevel * 100).toInt()}%", color = TextPrimary, fontSize = 12.sp)
    }
}

@Composable
private fun StreamHealthBadge(
    health: StreamHealth,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (health) {
        StreamHealth.POOR -> "⚠ Poor connection" to Warning
        StreamHealth.RECONNECTING -> "🔄 Reconnecting…" to AccentCyan
        StreamHealth.FAILED -> "❌ Stream failed" to ErrorRed
        else -> return
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xAA000000))
            .border(0.5.dp, color.copy(0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TorrentStatsOverlay(
    status: com.streamvault.app.domain.model.TorrentStatus,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xAA000000))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text("⬇ ${status.downloadSpeedBps.toSpeedString()}", color = TextSecondary, fontSize = 10.sp)
        Text("👤 ${status.seeds} seeds", color = TextSecondary, fontSize = 10.sp)
        Text("🔗 ${status.peers} peers", color = TextSecondary, fontSize = 10.sp)
        if (status.etaSeconds > 0) {
            Text("ETA ${status.etaSeconds.toEtaString()}", color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun NextEpisodeBanner(
    countdown: Int,
    onPlay: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BlackCard.copy(alpha = 0.95f))
            .border(1.dp, BlackBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Next Episode", color = TextTertiary, fontSize = 11.sp)
            Text("Playing in ${countdown}s…", color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
        Button(
            onClick = onPlay,
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Play Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
        }
    }
}
