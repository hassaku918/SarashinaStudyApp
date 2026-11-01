package com.sarashina.study.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightPrimary = Color(0xFF1A73E8)
private val LightSecondary = Color(0xFF00ACC1)
private val LightBackground = Color(0xFFF8F9FA)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFE8F0FE)
private val LightOnBackground = Color(0xFF202124)
private val LightOnSurface = Color(0xFF202124)

private val DarkPrimary = Color(0xFF8AB4F8)
private val DarkSecondary = Color(0xFF4DD0E1)
private val DarkBackground = Color(0xFF0F1115)
private val DarkSurface = Color(0xFF1A1D24)
private val DarkSurfaceVariant = Color(0xFF252A33)
private val DarkOnBackground = Color(0xFFE8EAED)
private val DarkOnSurface = Color(0xFFE8EAED)
private val DarkOnPrimary = Color(0xFF0D47A1)

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    secondary = LightSecondary,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF5F6368),
    error = Color(0xFFD93025),
    onError = Color.White,
    outline = Color(0xFFDADCE0)
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = Color(0xFF00363A),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB0B3B8),
    error = Color(0xFFF28B82),
    onError = Color(0xFF601410),
    outline = Color(0xFF3C4043)
)

enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        fun fromString(value: String): ThemeMode = when (value.lowercase()) {
            "light" -> LIGHT
            "dark" -> DARK
            else -> SYSTEM
        }
    }
}

@Composable
fun SarashinaStudyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
