package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NovaDarkColorScheme = darkColorScheme(
    primary = NovaElectricPurple,
    onPrimary = Color.White,
    primaryContainer = NovaDarkSurfaceVariant,
    onPrimaryContainer = NovaCyanBright,
    secondary = NovaVividBlue,
    onSecondary = Color.White,
    secondaryContainer = NovaDarkSurfaceVariant,
    onSecondaryContainer = Color.White,
    tertiary = NovaCyanGlow,
    onTertiary = Color.Black,
    background = NovaDarkBg,
    onBackground = NovaTextPrimary,
    surface = NovaDarkSurface,
    onSurface = NovaTextPrimary,
    surfaceVariant = NovaDarkSurfaceVariant,
    onSurfaceVariant = NovaTextSecondary,
    outline = NovaDarkBorder,
    error = NovaError,
    onError = Color.White
)

@Composable
fun NovaAiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NovaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
