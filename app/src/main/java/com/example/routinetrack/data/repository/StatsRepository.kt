package com.example.routinetrack.data.repository

import com.example.routinetrack.data.local.dao.HabitCompletionDao
import com.example.routinetrack.data.local.dao.HabitDao
import com.example.routinetrack.data.local.dao.UserDao
import com.example.routinetrack.data.mapper.toDomain
import com.example.routinetrack.domain.model.HabitCategory
import com.example.routinetrack.domain.model.HabitStats
import com.example.routinetrack.domain.usecase.CalculateHabitStatsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class StatsRepository(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val userDao: UserDao,
    private val calculateHabitStats: CalculateHabitStatsUseCase = CalculateHabitStatsUseCase()
) {
    // Le statistiche sono calcolate nel data layer, non nei composable.
    // In questo modo StatsScreen resta solo una vista e la logica è testabile.
    fun observeStats(): Flow<HabitStats> {
        return userDao.getLoggedUser().flatMapLatest { user ->
            val userId = user?.remoteId ?: return@flatMapLatest flowOf(HabitStats())
            combine(
                habitDao.observeHabitsForUser(userId),
                completionDao.observeCompletionsForUser(userId)
            ) { habits, completions ->
                calculateHabitStats(
                    habits = habits.map { it.toDomain() },
                    completions = completions.map { it.toDomain() }
                )
            }
        }
    }

    fun observeStatsForCategory(category: HabitCategory?): Flow<HabitStats> {
        return userDao.getLoggedUser().flatMapLatest { user ->
            val userId = user?.remoteId ?: return@flatMapLatest flowOf(HabitStats())
            combine(
                habitDao.observeHabitsForUser(userId),
                completionDao.observeCompletionsForUser(userId)
            ) { habits, completions ->
                val domainHabits = habits.map { it.toDomain() }
                    .filter { category == null || it.category == category }
                val habitIds = domainHabits.map { it.id }.toSet()
                calculateHabitStats(
                    habits = domainHabits,
                    completions = completions.map { it.toDomain() }
                        .filter { it.habitId in habitIds }
                )
            }
        }
    }

    fun observeStatsForHabit(habitId: Long): Flow<HabitStats> {
        return combine(
            habitDao.getHabitById(habitId),
            completionDao.getCompletionsForHabit(habitId)
        ) { habit, completions ->
            calculateHabitStats(
                habits = listOfNotNull(habit?.toDomain()),
                completions = completions.map { it.toDomain() }
            )
        }
    }

}
