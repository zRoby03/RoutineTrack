package com.example.routinetrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.routinetrack.domain.model.AppThemeMode

private val LightColors = lightColorScheme(
    primary = KhakiPrimary,
    onPrimary = RoutineTextPrimary,
    primaryContainer = KhakiSoft,
    onPrimaryContainer = RoutineTextPrimary,
    secondary = SageSuccess,
    onSecondary = Color.White,
    secondaryContainer = SageSoft,
    onSecondaryContainer = RoutineTextPrimary,
    tertiary = KhakiPrimaryDark,
    background = WarmBackground,
    onBackground = RoutineTextPrimary,
    surface = RoutineSurface,
    onSurface = RoutineTextPrimary,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = RoutineTextSecondary,
    outline = WarmDivider,
    error = SoftRed,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD6C28A),
    onPrimary = Color(0xFF1E1B14),
    primaryContainer = Color(0xFF3E3521),
    onPrimaryContainer = Color(0xFFF4E9C8),
    secondary = SageSuccess,
    onSecondary = Color(0xFF10150E),
    secondaryContainer = Color(0xFF273124),
    onSecondaryContainer = Color(0xFFE6F0E2),
    tertiary = KhakiPrimaryDark,
    background = Color(0xFF101112),
    onBackground = Color(0xFFF8F3E7),
    surface = Color(0xFF1B1C1E),
    onSurface = Color(0xFFF8F3E7),
    surfaceVariant = Color(0xFF242528),
    onSurfaceVariant = Color(0xFFCFC6B4),
    outline = Color(0xFF4A4336),
    error = Color(0xFFE19A90),
    onError = Color(0xFF2A0D08)
)

@Composable
fun RoutineTrackTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (themeMode == AppThemeMode.DARK) DarkColors else LightColors,
        typography = RoutineTypography,
        shapes = RoutineShapes,
        content = content
    )
}

fun pastelColorFromHex(hex: String): Color {
    return runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrDefault(KhakiSoft)
}
