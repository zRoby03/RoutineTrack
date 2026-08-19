package com.example.routinetrack.domain.model

enum class AppThemeMode(val storageValue: String, val label: String) {
    LIGHT("light", "Tema chiaro"),
    DARK("dark", "Tema scuro");

    companion object {
        fun fromStorage(value: String?): AppThemeMode {
            return entries.firstOrNull { it.storageValue == value } ?: LIGHT
        }
    }
}
