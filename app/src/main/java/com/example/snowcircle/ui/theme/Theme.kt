package com.example.snowcircle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = SnowPrimary,
    secondary = SnowSecondary,
    surface = SnowSurface,
    background = Color.White,
    error = SnowError
)

private val DarkColors = darkColorScheme(
    primary = SnowPrimary,
    secondary = SnowSecondary,
    surface = Color(0xFF1B1B1B),
    background = Color(0xFF101010),
    error = SnowError
)

@Composable
fun SnowTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
