package com.example.routinetrack.domain.model

import java.time.LocalDate
import java.time.YearMonth

data class Habit(
    val id: Long = 0,
    val remoteId: String? = null,
    val userId: String = "",
    val title: String,
    val description: String,
    val category: HabitCategory,
    val color: String,
    val type: HabitType,
    val targetValue: Double? = null,
    val unit: String? = null,
    val frequency: HabitFrequency = HabitFrequency.daily(),
    val reminderEnabled: Boolean = false,
    val reminderTime: String? = null,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val startDate: String = LocalDate.now().toString(),
    val endDate: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val pendingSync: Boolean = true
)

fun Habit.isActiveOn(date: LocalDate): Boolean {
    val start = runCatching { LocalDate.parse(startDate) }.getOrNull()
    val end = endDate?.let { value ->
        runCatching { LocalDate.parse(value) }.getOrNull()
    }
    val afterStart = start == null || !date.isBefore(start)
    val beforeEnd = end == null || !date.isAfter(end)
    return afterStart && beforeEnd
}

fun Habit.isActiveDuring(month: YearMonth): Boolean {
    val firstDay = month.atDay(1)
    val lastDay = month.atEndOfMonth()
    val start = runCatching { LocalDate.parse(startDate) }.getOrNull()
    val end = endDate?.let { value ->
        runCatching { LocalDate.parse(value) }.getOrNull()
    }
    val startsBeforeMonthEnds = start == null || !start.isAfter(lastDay)
    val endsAfterMonthStarts = end == null || !end.isBefore(firstDay)
    return startsBeforeMonthEnds && endsAfterMonthStarts
}
