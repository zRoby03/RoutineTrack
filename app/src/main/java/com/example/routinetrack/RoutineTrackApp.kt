package com.example.routinetrack

import androidx.compose.runtime.Composable
import com.example.routinetrack.domain.model.AppThemeMode
import com.example.routinetrack.navigation.RoutineTrackNavigation

@Composable
fun RoutineTrackApp(
    currentThemeMode: AppThemeMode,
    onThemeModeChanged: (AppThemeMode) -> Unit,
    onLauncherIconApply: () -> Unit
) {
    RoutineTrackNavigation(
        currentThemeMode = currentThemeMode,
        onThemeModeChanged = onThemeModeChanged,
        onLauncherIconApply = onLauncherIconApply
    )
}
