package com.example.routinetrack

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.routinetrack.domain.model.AppThemeMode
import com.example.routinetrack.notification.NotificationHelper
import com.example.routinetrack.ui.theme.RoutineTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)

        setContent {
            val app = application as RoutineTrackApplication
            val themePreferenceRepository = remember { app.container.themePreferenceRepository }
            var themeMode by rememberSaveable {
                mutableStateOf(themePreferenceRepository.getThemeMode())
            }

            RoutineTrackTheme(themeMode = themeMode) {
                val backgroundColor = MaterialTheme.colorScheme.background
                val useDarkStatusIcons = themeMode == AppThemeMode.LIGHT
                SideEffect {
                    window.statusBarColor = backgroundColor.toArgbCompat()
                    window.navigationBarColor = backgroundColor.toArgbCompat()
                    WindowCompat.setDecorFitsSystemWindows(window, true)
                    WindowInsetsControllerCompat(window, window.decorView).apply {
                        isAppearanceLightStatusBars = useDarkStatusIcons
                        isAppearanceLightNavigationBars = useDarkStatusIcons
                    }
                }
                RoutineTrackApp(
                    currentThemeMode = themeMode,
                    onThemeModeChanged = { newThemeMode ->
                        themeMode = newThemeMode
                        themePreferenceRepository.saveThemeMode(newThemeMode)
                    },
                    onLauncherIconApply = themePreferenceRepository::applySavedLauncherIcon
                )
            }
        }
    }

    private fun androidx.compose.ui.graphics.Color.toArgbCompat(): Int {
        return Color.argb(
            (alpha * 255).toInt(),
            (red * 255).toInt(),
            (green * 255).toInt(),
            (blue * 255).toInt()
        )
    }
}
