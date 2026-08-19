package com.example.routinetrack.domain.model

enum class HabitUnit(val label: String) {
    ML("ml"),
    L("L"),
    TIMES("Volte"),
    PAGES("Pagine"),
    SETS("Serie"),
    GRAMS("Grammi"),
    TIME("Tempo");

    companion object {
        fun fromLabel(label: String?): HabitUnit {
            val normalized = label?.trim()
            return entries.firstOrNull { it.label.equals(normalized, ignoreCase = true) }
                ?: when (normalized?.lowercase()) {
                    "grams", "grammi" -> GRAMS
                    else -> TIMES
                }
        }

        fun displayLabel(label: String?): String {
            val normalized = label?.trim().orEmpty()
            if (normalized.isBlank()) return ""
            return entries.firstOrNull { it.label.equals(normalized, ignoreCase = true) }?.label
                ?: when (normalized.lowercase()) {
                    "grams", "grammi" -> GRAMS.label
                    else -> normalized
                }
        }
    }
}
