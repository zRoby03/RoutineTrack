package com.example.routinetrack.domain.model

enum class AppThemeMode(val storageValue: String, val label: String) {
    LIGHT("light", "Light Theme"),
    DARK("dark", "Dark Theme");

    companion object {
        fun fromStorage(value: String?): AppThemeMode {
            return entries.firstOrNull { it.storageValue == value } ?: LIGHT
        }
    }
}
