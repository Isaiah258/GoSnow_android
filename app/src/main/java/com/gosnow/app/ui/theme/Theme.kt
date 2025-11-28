package com.gosnow.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// 深色模式下的颜色（大致偏黑）
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF9FAFB),
    onPrimary = Color(0xFF111827),
    secondary = Color(0xFF9CA3AF),
    onSecondary = Color(0xFF020617),
    background = Color(0xFF020617),
    onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF020617),
    onSurface = Color(0xFFE5E7EB)
)

// 浅色模式：整体偏白
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF111827),
    onPrimary = Color.White,
    secondary = Color(0xFF4B5563),
    onSecondary = Color.White,
    background = Color(0xFFF9FAFB),
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827)
)

@Composable
fun GosnowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 为了让默认是“白色系”，这里默认关闭动态配色
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
