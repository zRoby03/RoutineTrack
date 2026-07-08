package com.example.routinetrack.domain.usecase

import java.time.LocalDate

class CalculateBestStreakUseCase {
    operator fun invoke(completedDates: Set<LocalDate>): Int {
        if (completedDates.isEmpty()) return 0

        val sortedDates = completedDates.sorted()
        var best = 1
        var current = 1

        for (index in 1 until sortedDates.size) {
            val previous = sortedDates[index - 1]
            val currentDate = sortedDates[index]
            if (currentDate == previous.plusDays(1)) {
                current++
            } else {
                best = maxOf(best, current)
                current = 1
            }
        }

        return maxOf(best, current)
    }
}
