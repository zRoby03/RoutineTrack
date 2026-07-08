package com.example.routinetrack.domain.usecase

import com.example.routinetrack.domain.model.Habit
import com.example.routinetrack.domain.model.HabitCompletion
import com.example.routinetrack.domain.model.HabitStats
import com.example.routinetrack.domain.model.WeeklyHabitReport
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class CalculateHabitStatsUseCase(
    private val calculateCurrentStreak: CalculateCurrentStreakUseCase = CalculateCurrentStreakUseCase(),
    private val calculateBestStreak: CalculateBestStreakUseCase = CalculateBestStreakUseCase(),
    private val suggestHabitRisk: SuggestHabitRiskUseCase = SuggestHabitRiskUseCase()
) {
    operator fun invoke(
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        referenceDate: LocalDate = LocalDate.now()
    ): HabitStats {
        val completed = completions.filter { it.completed }
        val completedDates = completed
            .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
            .toSet()

        val weekStart = referenceDate.minusDays(6)
        val monthStart = referenceDate.withDayOfMonth(1)
        val yearStart = referenceDate.withDayOfYear(1)

        val weeklyRate = completionRate(habits, completed, weekStart, referenceDate)
        val monthlyRate = completionRate(habits, completed, monthStart, referenceDate)
        val completionCounts = completed.groupingBy { it.habitId }.eachCount()

        val mostCompletedHabit = habits.maxByOrNull { completionCounts[it.id] ?: 0 }
            ?.takeIf { (completionCounts[it.id] ?: 0) > 0 }
            ?.title
        val leastCompletedHabit = habits.minByOrNull { completionCounts[it.id] ?: 0 }
            ?.title

        val annualHeatmap = completed
            .mapNotNull { completion ->
                runCatching { LocalDate.parse(completion.date) }.getOrNull()?.let { date ->
                    if (!date.isBefore(yearStart) && !date.isAfter(referenceDate)) {
                        date.toString()
                    } else {
                        null
                    }
                }
            }
            .groupingBy { it }
            .eachCount()

        val bestDayName = completed
            .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
            .groupingBy { it.dayOfWeek }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?.getDisplayName(TextStyle.FULL, Locale.ITALIAN)

        return HabitStats(
            weeklyCompletionRate = weeklyRate,
            monthlyCompletionRate = monthlyRate,
            currentStreak = calculateCurrentStreak(completedDates, referenceDate),
            bestStreak = calculateBestStreak(completedDates),
            totalCompletions = completed.size,
            mostCompletedHabit = mostCompletedHabit,
            leastCompletedHabit = leastCompletedHabit,
            atRiskHabits = suggestHabitRisk(habits, completions, referenceDate),
            bestDayName = bestDayName,
            annualHeatmap = annualHeatmap,
            weeklyRows = weeklyRows(habits, completed, weekStart, referenceDate)
        )
    }

    private fun completionRate(
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        start: LocalDate,
        end: LocalDate
    ): Int {
        val expected = habits.sumOf { habit -> expectedDates(habit, start, end).size }
        if (expected == 0) return 0

        val completionKeys = completions
            .mapNotNull { completion ->
                val date = runCatching { LocalDate.parse(completion.date) }.getOrNull()
                date?.takeIf { !it.isBefore(start) && !it.isAfter(end) }
                    ?.let { "${completion.habitId}|$it" }
            }
            .toSet()

        return ((completionKeys.size.toDouble() / expected.toDouble()) * 100).toInt()
            .coerceIn(0, 100)
    }

    private fun weeklyRows(
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        start: LocalDate,
        end: LocalDate
    ): List<WeeklyHabitReport> {
        return habits.map { habit ->
            val expected = expectedDates(habit, start, end)
            val completedDays = completions
                .filter { it.habitId == habit.id }
                .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
                .filter { expected.contains(it) }
                .toSet()
                .size
            WeeklyHabitReport(
                habitTitle = habit.title,
                completedDays = completedDays,
                expectedDays = expected.size,
                rate = if (expected.isEmpty()) 0 else ((completedDays.toDouble() / expected.size) * 100).toInt()
            )
        }
    }

    private fun expectedDates(habit: Habit, start: LocalDate, end: LocalDate): Set<LocalDate> {
        val result = mutableSetOf<LocalDate>()
        var cursor = start
        while (!cursor.isAfter(end)) {
            if (habit.frequency.includes(cursor.dayOfWeek)) {
                result += cursor
            }
            cursor = cursor.plusDays(1)
        }
        return result
    }
}
