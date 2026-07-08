package com.example.routinetrack.data.preferences

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.routinetrack.domain.model.AppThemeMode

class ThemePreferenceRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getThemeMode(): AppThemeMode {
        return AppThemeMode.fromStorage(prefs.getString(KEY_THEME_MODE, null))
    }

    fun saveThemeMode(mode: AppThemeMode) {
        prefs.edit()
            .putString(KEY_THEME_MODE, mode.storageValue)
            .apply()
    }

    fun applySavedLauncherIcon() {
        applyLauncherIcon(getThemeMode())
    }

    private fun applyLauncherIcon(mode: AppThemeMode) {
        val packageManager = appContext.packageManager
        val selectedAlias = mode.aliasClassName()
        val disabledAlias = if (mode == AppThemeMode.LIGHT) {
            AppThemeMode.DARK.aliasClassName()
        } else {
            AppThemeMode.LIGHT.aliasClassName()
        }

        setAliasState(
            packageManager = packageManager,
            aliasClassName = selectedAlias,
            enabledState = PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        )
        setAliasState(
            packageManager = packageManager,
            aliasClassName = disabledAlias,
            enabledState = PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        )
    }

    private fun setAliasState(
        packageManager: PackageManager,
        aliasClassName: String,
        enabledState: Int
    ) {
        runCatching {
            packageManager.setComponentEnabledSetting(
                ComponentName(appContext.packageName, aliasClassName),
                enabledState,
                PackageManager.DONT_KILL_APP
            )
        }
    }

    private fun AppThemeMode.aliasClassName(): String {
        return when (this) {
            AppThemeMode.LIGHT -> "${appContext.packageName}.MainActivityLightAlias"
            AppThemeMode.DARK -> "${appContext.packageName}.MainActivityDarkAlias"
        }
    }

    private companion object {
        const val PREFS_NAME = "routine_theme_preferences"
        const val KEY_THEME_MODE = "theme_mode"
    }
}
