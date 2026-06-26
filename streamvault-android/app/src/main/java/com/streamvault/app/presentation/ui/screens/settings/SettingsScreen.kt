package com.streamvault.app.presentation.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.streamvault.app.domain.model.StreamAddon
import com.streamvault.app.presentation.ui.theme.*
import com.streamvault.app.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
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
                .background(
                    Brush.verticalGradient(colors = listOf(BlackSurface, AmoledBlack))
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                "Settings",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsSectionCard(title = "🔌 Stream Addons") {
                    addons.forEach { addon ->
                        AddonToggleRow(
                            addon = addon,
                            enabled = addonStates[addon.id] ?: addon.isEnabled,
                            onToggle = { enabled ->
                                addonStates = addonStates + (addon.id to enabled)
                                viewModel.toggleAddon(addon.id, enabled)
                            }
                        )
                    }
                }
            }

            item {
                SettingsSectionCard(title = "🎮 Player") {
                    SettingsInfoRow("Default Quality", "1080p", Icons.Default.Hd)
                    SettingsDivider()
                    SettingsInfoRow("Audio Language", "English", Icons.Default.VolumeUp)
                    SettingsDivider()
                    SettingsInfoRow("Subtitle Language", "English", Icons.Default.Subtitles)
                    SettingsDivider()
                    SettingsToggleRow("Auto-play Next Episode", true, Icons.Default.PlayCircle)
                    SettingsDivider()
                    SettingsToggleRow("Skip Intro", false, Icons.Default.FastForward)
                }
            }

            item {
                SettingsSectionCard(title = "📥 Downloads") {
                    SettingsInfoRow("Location", "Movies/StreamVault", Icons.Default.FolderOpen)
                    SettingsDivider()
                    SettingsToggleRow("Download over Wi-Fi only", true, Icons.Default.Wifi)
                    SettingsDivider()
                    SettingsInfoRow("Max parallel downloads", "3", Icons.Default.Download)
                }
            }

            item {
                SettingsSectionCard(title = "🎨 Appearance") {
                    SettingsInfoRow("Theme", "AMOLED Black", Icons.Default.DarkMode)
                    SettingsDivider()
                    SettingsInfoRow("Subtitle Size", "Medium", Icons.Default.TextFields)
                }
            }

            item {
                SettingsSectionCard(title = "ℹ️ About") {
                    SettingsInfoRow("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", Icons.Default.Info)
                    SettingsDivider()
                    SettingsInfoRow("Build", if (BuildConfig.DEBUG) "Debug" else "Release", Icons.Default.Build)
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SettingsSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BlackCard)
            .border(0.5.dp, BlackBorder, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Brush.verticalGradient(listOf(AccentRed, Color(0xFFFF6B35))))
            )
            Text(
                title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        content()
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(0.5.dp)
            .background(BlackBorder)
    )
}

@Composable
fun AddonToggleRow(addon: StreamAddon, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dotColor = when (addon.id) {
                "torrentio" -> Color(0xFFFF6B35)
                "knightcrawler" -> Color(0xFF9C27B0)
                "mediafusion" -> Color(0xFF2196F3)
                "comet" -> Color(0xFF00BCD4)
                "jackettio" -> Color(0xFF4CAF50)
                "cinemeta" -> Color(0xFFFF9800)
                else -> TextTertiary
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (enabled) dotColor else TextDisabled)
            )
            Column {
                Text(addon.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    addon.baseUrl.removePrefix("https://"),
                    color = TextTertiary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = AccentRed,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = BlackElevated,
                uncheckedBorderColor = BlackBorder
            )
        )
    }
}

@Composable
fun SettingsInfoRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BlackElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp))
        }
        Text(label, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(
            value,
            color = TextTertiary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Icon(Icons.Default.ChevronRight, null, tint = TextDisabled, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun SettingsToggleRow(label: String, defaultValue: Boolean, icon: ImageVector) {
    var checked by remember { mutableStateOf(defaultValue) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { checked = !checked }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BlackElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp))
        }
        Text(label, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = AccentRed,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = BlackElevated,
                uncheckedBorderColor = BlackBorder
            )
        )
    }
}
