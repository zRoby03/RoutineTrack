package com.example.routinetrack.domain.model

import java.time.DayOfWeek

enum class FrequencyMode {
    DAILY,
    SPECIFIC_DAYS
}

data class HabitFrequency(
    val mode: FrequencyMode = FrequencyMode.DAILY,
    val daysOfWeek: Set<Int> = allDays
) {
    fun includes(dayOfWeek: DayOfWeek): Boolean {
        return mode == FrequencyMode.DAILY || daysOfWeek.contains(dayOfWeek.value)
    }

    fun toStorage(): String {
        return when (mode) {
            FrequencyMode.DAILY -> DAILY_STORAGE
            FrequencyMode.SPECIFIC_DAYS -> daysOfWeek.sorted().joinToString(
                prefix = SPECIFIC_PREFIX,
                separator = ","
            )
        }
    }

    companion object {
        private const val DAILY_STORAGE = "DAILY"
        private const val SPECIFIC_PREFIX = "SPECIFIC:"
        val allDays: Set<Int> = (1..7).toSet()

        fun daily(): HabitFrequency = HabitFrequency()

        fun specific(days: Set<Int>): HabitFrequency {
            return HabitFrequency(
                mode = FrequencyMode.SPECIFIC_DAYS,
                daysOfWeek = days.ifEmpty { allDays }
            )
        }

        fun fromStorage(raw: String): HabitFrequency {
            if (raw == DAILY_STORAGE || raw.isBlank()) return daily()
            if (!raw.startsWith(SPECIFIC_PREFIX)) return daily()

            val days = raw.removePrefix(SPECIFIC_PREFIX)
                .split(",")
                .mapNotNull { it.toIntOrNull() }
                .filter { it in 1..7 }
                .toSet()
            return specific(days)
        }
    }
}
