package com.example.routinetrack.domain.usecase

import java.time.LocalDate

class CalculateCurrentStreakUseCase {
    operator fun invoke(
        completedDates: Set<LocalDate>,
        referenceDate: LocalDate = LocalDate.now()
    ): Int {
        var streak = 0
        var cursor = referenceDate

        while (completedDates.contains(cursor)) {
            streak++
            cursor = cursor.minusDays(1)
        }

        return streak
    }
}
