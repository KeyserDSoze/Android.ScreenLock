package com.keysersoze.screenlock.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val ScreenLockColors = darkColorScheme(
    primary = Color(0xFFB8F3D2),
    onPrimary = Color(0xFF092017),
    primaryContainer = Color(0xFF17382A),
    onPrimaryContainer = Color(0xFFD8FFE8),
    secondary = Color(0xFFB9C8FF),
    onSecondary = Color(0xFF111A39),
    background = Color(0xFF090C11),
    onBackground = Color(0xFFF4F7FA),
    surface = Color(0xFF11161D),
    onSurface = Color(0xFFF4F7FA),
    surfaceVariant = Color(0xFF1A212B),
    onSurfaceVariant = Color(0xFFBEC7D2),
    outline = Color(0xFF657180),
)

@Composable
fun ScreenLockTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ScreenLockColors) {
        CompositionLocalProvider(
            LocalContentColor provides ScreenLockColors.onBackground,
            content = content,
        )
    }
}
