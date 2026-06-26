package com.streamvault.app.presentation.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        Text(
            "Settings",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsSection(title = "Stream Addons") {
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
                SettingsSection(title = "Player") {
                    SettingsInfoRow("Default Quality", "1080p", Icons.Default.Hd)
                    SettingsInfoRow("Preferred Audio", "English", Icons.Default.VolumeUp)
                    SettingsInfoRow("Preferred Subtitles", "English", Icons.Default.Subtitles)
                    SettingsToggleRow("Auto-play Next Episode", true, Icons.Default.PlayCircle)
                }
            }

            item {
                SettingsSection(title = "Downloads") {
                    SettingsInfoRow("Download Location", "Movies/StreamVault", Icons.Default.FolderOpen)
                }
            }

            item {
                SettingsSection(title = "About") {
                    SettingsInfoRow("Version", BuildConfig.VERSION_NAME, Icons.Default.Info)
                    SettingsInfoRow("Build", if (BuildConfig.DEBUG) "Debug" else "Release", Icons.Default.Build)
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BlackCard)
    ) {
        Text(
            title,
            color = AccentRed,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun AddonToggleRow(addon: StreamAddon, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(addon.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(addon.baseUrl, color = TextTertiary, fontSize = 11.sp, maxLines = 1)
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = AccentRed,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = BlackElevated
            )
        )
    }
}

@Composable
fun SettingsInfoRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(20.dp))
        Text(label, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = TextTertiary, fontSize = 13.sp)
    }
}

@Composable
fun SettingsToggleRow(label: String, defaultValue: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    var checked by remember { mutableStateOf(defaultValue) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(20.dp))
        Text(label, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = AccentRed
            )
        )
    }
}
