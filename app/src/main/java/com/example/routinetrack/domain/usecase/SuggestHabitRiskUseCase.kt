package com.example.routinetrack.domain.usecase

import com.example.routinetrack.domain.model.Habit
import com.example.routinetrack.domain.model.HabitCompletion
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class SuggestHabitRiskUseCase {
    operator fun invoke(
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        referenceDate: LocalDate = LocalDate.now(),
        riskAfterDays: Long = 3
    ): List<String> {
        return habits.mapNotNull { habit ->
            val completedDates = completions
                .filter { it.habitId == habit.id && it.completed }
                .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }

            val lastCompletion = completedDates.maxOrNull()
            val fallbackDate = Instant.ofEpochMilli(habit.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val lastActiveDate = lastCompletion ?: fallbackDate
            val daysMissing = java.time.temporal.ChronoUnit.DAYS.between(lastActiveDate, referenceDate)

            if (daysMissing >= riskAfterDays) {
                "${habit.title} e a rischio: non la completi da $daysMissing giorni."
            } else {
                null
            }
        }
    }
}
