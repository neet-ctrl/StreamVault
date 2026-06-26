package com.streamvault.app.presentation.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamvault.app.BuildConfig
import com.streamvault.app.engine.SubtitleManager
import com.streamvault.app.presentation.ui.theme.*
import com.streamvault.app.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings
    val subtitlePrefs = uiState.subtitlePrefs
    val addons = remember { viewModel.addons }
    var addonStates by remember { mutableStateOf(addons.associate { it.id to it.isEnabled }) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .statusBarsPadding()
    ) {
        // Premium header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(BlackSurface, AmoledBlack)))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentRed.copy(0.15f))
                        .border(1.dp, AccentRed.copy(0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, null, tint = AccentRed, modifier = Modifier.size(18.dp))
                }
                Text("Settings", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {

            // ─── Playback ───
            item { SettingsSectionLabel("Playback") }

            item {
                // Default Quality
                var showQualityPicker by remember { mutableStateOf(false) }
                SettingsRow(
                    icon = Icons.Default.HighQuality,
                    label = "Default Quality",
                    value = settings.preferredQuality,
                    onClick = { showQualityPicker = true }
                )
                if (showQualityPicker) {
                    SettingsPickerDialog(
                        title = "Default Quality",
                        options = listOf("4K", "1080p", "720p", "480p", "Best Available"),
                        selected = settings.preferredQuality,
                        onSelect = { viewModel.setPreferredQuality(it); showQualityPicker = false },
                        onDismiss = { showQualityPicker = false }
                    )
                }
            }

            item {
                SettingsToggleRow(
                    icon = Icons.Default.SkipNext,
                    label = "Auto-play Next Episode",
                    checked = settings.autoPlayNext,
                    onToggle = viewModel::setAutoPlayNext
                )
            }

            item {
                SettingsToggleRow(
                    icon = Icons.Default.Memory,
                    label = "Hardware Acceleration",
                    checked = settings.hardwareAcceleration,
                    onToggle = viewModel::setHardwareAcceleration,
                    subtitle = "Enables GPU-accelerated video decoding"
                )
            }

            item {
                SettingsToggleRow(
                    icon = Icons.Default.OpenInNew,
                    label = "External Player Support",
                    checked = settings.useExternalPlayer,
                    onToggle = viewModel::setUseExternalPlayer,
                    subtitle = "Open streams in VLC, MX Player, etc."
                )
            }

            // ─── Torrent / Streaming ───
            item { SettingsSectionLabel("Torrent & Streaming") }

            item {
                SettingsSliderRow(
                    icon = Icons.Default.CloudDownload,
                    label = "Pre-buffer Size",
                    value = settings.preBufferSizeMb.toFloat(),
                    valueRange = 5f..100f,
                    steps = 18,
                    displayValue = "${settings.preBufferSizeMb} MB",
                    onValueChange = { viewModel.setPreBufferSize(it.toInt()) }
                )
            }

            item {
                SettingsSliderRow(
                    icon = Icons.Default.Link,
                    label = "Max Connections",
                    value = settings.maxConnections.toFloat(),
                    valueRange = 50f..500f,
                    steps = 8,
                    displayValue = "${settings.maxConnections}",
                    onValueChange = { viewModel.setMaxConnections(it.toInt()) }
                )
            }

            item {
                SettingsSliderRow(
                    icon = Icons.Default.Timer,
                    label = "Torrent Timeout",
                    value = settings.torrentTimeoutSeconds.toFloat(),
                    valueRange = 10f..120f,
                    steps = 10,
                    displayValue = "${settings.torrentTimeoutSeconds}s",
                    onValueChange = { viewModel.setTorrentTimeout(it.toInt()) }
                )
            }

            // ─── Subtitles ───
            item { SettingsSectionLabel("Subtitles") }

            item {
                var showLangPicker by remember { mutableStateOf(false) }
                SettingsRow(
                    icon = Icons.Default.Subtitles,
                    label = "Subtitle Language",
                    value = subtitlePrefs.language,
                    onClick = { showLangPicker = true }
                )
                if (showLangPicker) {
                    SettingsPickerDialog(
                        title = "Subtitle Language",
                        options = SubtitleManager.SUPPORTED_LANGUAGES.keys.toList(),
                        selected = subtitlePrefs.language,
                        onSelect = { lang ->
                            val code = SubtitleManager.SUPPORTED_LANGUAGES[lang] ?: "en"
                            viewModel.setSubtitleLanguage(lang, code)
                            showLangPicker = false
                        },
                        onDismiss = { showLangPicker = false }
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BlackCard)
                        .border(0.5.dp, BlackBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.TextFields, null, tint = AccentRed, modifier = Modifier.size(18.dp))
                        Text("Subtitle Size", color = TextSecondary, fontSize = 14.sp)
                        Spacer(Modifier.weight(1f))
                        Text("${(subtitlePrefs.sizeScale * 100).toInt()}%", color = TextTertiary, fontSize = 12.sp)
                    }
                    Slider(
                        value = subtitlePrefs.sizeScale,
                        onValueChange = viewModel::setSubtitleSize,
                        valueRange = 0.5f..2.5f,
                        colors = SliderDefaults.colors(thumbColor = AccentRed, activeTrackColor = AccentRed, inactiveTrackColor = BlackBorder)
                    )
                }
            }

            item {
                SettingsToggleRow(
                    icon = Icons.Default.FormatBold,
                    label = "Bold Subtitles",
                    checked = subtitlePrefs.boldEnabled,
                    onToggle = viewModel::setSubtitleBold
                )
            }

            item {
                SettingsToggleRow(
                    icon = Icons.Default.BorderColor,
                    label = "Subtitle Outline",
                    checked = subtitlePrefs.outlineEnabled,
                    onToggle = viewModel::setSubtitleOutline
                )
            }

            // ─── Addon Sources ───
            item { SettingsSectionLabel("Stream Addons") }

            items(addons) { addon ->
                val enabled = addonStates[addon.id] != false
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BlackCard)
                        .border(0.5.dp, BlackBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (addon.id) {
                                    "torrentio" -> AccentRed
                                    "mediafusion" -> Color(0xFF9C27B0)
                                    "knightcrawler" -> AccentCyan
                                    "comet" -> Color(0xFFFF9800)
                                    "jackettio" -> Color(0xFF4CAF50)
                                    else -> TextTertiary
                                }.let { if (enabled) it else TextDisabled }
                            )
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(addon.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(
                            "P${addon.priority} · ${if (addon.supportsMovies) "Movies" else ""}${if (addon.supportsTv) " TV" else ""}",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { newEnabled ->
                            addonStates = addonStates + (addon.id to newEnabled)
                            viewModel.toggleAddon(addon.id, newEnabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentRed,
                            checkedTrackColor = AccentRed.copy(0.3f),
                            uncheckedThumbColor = TextDisabled,
                            uncheckedTrackColor = BlackBorder
                        )
                    )
                }
            }

            // ─── Data ───
            item { SettingsSectionLabel("Data & Cache") }

            item {
                SettingsActionRow(
                    icon = Icons.Default.History,
                    label = "Clear Watch History",
                    actionLabel = "Clear",
                    isDestructive = true,
                    onClick = viewModel::clearHistory
                )
            }

            item {
                SettingsActionRow(
                    icon = Icons.Default.CleaningServices,
                    label = "Clear Subtitle Cache",
                    actionLabel = "Clear",
                    isDestructive = false,
                    onClick = viewModel::clearSubtitleCache
                )
            }

            // ─── About ───
            item { SettingsSectionLabel("About") }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BlackCard)
                        .border(0.5.dp, BlackBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Version", color = TextSecondary, fontSize = 13.sp)
                        Text(BuildConfig.VERSION_NAME, color = TextTertiary, fontSize = 13.sp)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Build", color = TextSecondary, fontSize = 13.sp)
                        Text("${BuildConfig.VERSION_CODE}", color = TextTertiary, fontSize = 13.sp)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("TMDB Key", color = TextSecondary, fontSize = 13.sp)
                        Text("●●●●●●●●", color = TextTertiary, fontSize = 13.sp)
                    }
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable settings components
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SettingsSectionLabel(title: String) {
    Text(
        title.uppercase(),
        color = AccentRed,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp, start = 4.dp)
    )
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BlackCard)
            .border(0.5.dp, BlackBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AccentRed.copy(0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AccentRed, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(label, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = TextTertiary, fontSize = 13.sp)
        Spacer(Modifier.width(4.dp))
        Icon(Icons.Default.ChevronRight, null, tint = TextDisabled, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BlackCard)
            .border(0.5.dp, BlackBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AccentRed.copy(0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AccentRed, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = TextSecondary, fontSize = 14.sp)
            if (subtitle != null) {
                Text(subtitle, color = TextTertiary, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AccentRed,
                checkedTrackColor = AccentRed.copy(0.3f),
                uncheckedThumbColor = TextDisabled,
                uncheckedTrackColor = BlackBorder
            )
        )
    }
}

@Composable
fun SettingsSliderRow(
    icon: ImageVector,
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    displayValue: String,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BlackCard)
            .border(0.5.dp, BlackBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentRed.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = AccentRed, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(label, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text(displayValue, color = AccentRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = AccentRed,
                activeTrackColor = AccentRed,
                inactiveTrackColor = BlackBorder
            )
        )
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    label: String,
    actionLabel: String,
    isDestructive: Boolean,
    onClick: () -> Unit
) {
    var confirm by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BlackCard)
            .border(0.5.dp, BlackBorder, RoundedCornerShape(14.dp))
            .clickable { if (isDestructive) confirm = true else onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isDestructive) ErrorRed.copy(0.12f) else AccentRed.copy(0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = if (isDestructive) ErrorRed else AccentRed, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(label, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(actionLabel, color = if (isDestructive) ErrorRed else AccentRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            containerColor = BlackCard,
            shape = RoundedCornerShape(20.dp),
            title = { Text(label, color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This action cannot be undone.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { onClick(); confirm = false }) {
                    Text(actionLabel, color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) { Text("Cancel", color = TextTertiary) }
            }
        )
    }
}

@Composable
fun SettingsPickerDialog(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BlackCard,
        shape = RoundedCornerShape(20.dp),
        title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (option == selected) AccentRed.copy(0.15f) else Color.Transparent)
                            .clickable { onSelect(option) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selected,
                            onClick = { onSelect(option) },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentRed, unselectedColor = TextTertiary)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(option, color = if (option == selected) AccentRed else TextSecondary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done", color = AccentRed) }
        }
    )
}
