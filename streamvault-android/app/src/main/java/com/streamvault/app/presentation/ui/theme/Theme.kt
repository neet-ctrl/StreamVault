package com.streamvault.app.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val StreamVaultColorScheme = darkColorScheme(
    primary = AccentRed,
    onPrimary = TextPrimary,
    primaryContainer = BlackCard,
    onPrimaryContainer = TextPrimary,
    secondary = AccentOrange,
    onSecondary = TextPrimary,
    secondaryContainer = BlackElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentCyan,
    onTertiary = AmoledBlack,
    tertiaryContainer = BlackCard,
    onTertiaryContainer = TextPrimary,
    error = ErrorRed,
    onError = TextPrimary,
    errorContainer = Color(0xFF3E0000),
    onErrorContainer = ErrorRed,
    background = AmoledBlack,
    onBackground = TextPrimary,
    surface = BlackSurface,
    onSurface = TextPrimary,
    surfaceVariant = BlackCard,
    onSurfaceVariant = TextSecondary,
    surfaceTint = AccentRed,
    inverseSurface = TextPrimary,
    inverseOnSurface = AmoledBlack,
    inversePrimary = Color(0xFF9A1B1F),
    outline = BlackBorder,
    outlineVariant = GlassBorder,
    scrim = Color(0xCC000000)
)

@Composable
fun StreamVaultTheme(content: @Composable () -> Unit) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = false
        )
        systemUiController.setNavigationBarColor(
            color = Color(0xFF0A0A0A),
            darkIcons = false
        )
    }
    MaterialTheme(
        colorScheme = StreamVaultColorScheme,
        typography = StreamVaultTypography,
        shapes = StreamVaultShapes,
        content = content
    )
}
