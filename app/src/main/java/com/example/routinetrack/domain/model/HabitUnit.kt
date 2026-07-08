package com.example.routinetrack.domain.model

enum class HabitUnit(val label: String) {
    ML("ml"),
    L("L"),
    TIMES("Volte"),
    PAGES("Pagine"),
    SETS("Serie"),
    GRAMS("Grams"),
    TIME("Tempo");

    companion object {
        fun fromLabel(label: String?): HabitUnit {
            return entries.firstOrNull { it.label == label } ?: TIMES
        }
    }
}
