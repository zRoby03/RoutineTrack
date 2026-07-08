package com.example.routinetrack.domain.model

data class HabitStats(
    val weeklyCompletionRate: Int = 0,
    val monthlyCompletionRate: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val mostCompletedHabit: String? = null,
    val leastCompletedHabit: String? = null,
    val atRiskHabits: List<String> = emptyList(),
    val bestDayName: String? = null,
    val annualHeatmap: Map<String, Int> = emptyMap(),
    val weeklyRows: List<WeeklyHabitReport> = emptyList()
)

data class WeeklyHabitReport(
    val habitTitle: String,
    val completedDays: Int,
    val expectedDays: Int,
    val rate: Int
)
