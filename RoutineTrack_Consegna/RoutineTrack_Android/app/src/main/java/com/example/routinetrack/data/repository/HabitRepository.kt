package com.example.routinetrack.data.repository

import com.example.routinetrack.data.local.dao.HabitCompletionDao
import com.example.routinetrack.data.local.dao.HabitDao
import com.example.routinetrack.data.local.dao.UserDao
import com.example.routinetrack.data.local.entity.HabitCompletionEntity
import com.example.routinetrack.data.mapper.toDomain
import com.example.routinetrack.data.mapper.toEntity
import com.example.routinetrack.domain.model.Habit
import com.example.routinetrack.domain.model.HabitCompletion
import com.example.routinetrack.notification.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class HabitRepository(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val userDao: UserDao,
    private val reminderScheduler: ReminderScheduler
) {
    // Repository: nasconde Room ai ViewModel e applica sempre il filtro per utente loggato.
    fun observeHabits(): Flow<List<Habit>> {
        return userDao.getLoggedUser().flatMapLatest { user ->
            val userId = user?.remoteId ?: return@flatMapLatest flowOf(emptyList())
            habitDao.observeHabitsForUser(userId).map { habits -> habits.map { it.toDomain() } }
        }
    }

    fun observeHabit(habitId: Long): Flow<Habit?> {
        return userDao.getLoggedUser().flatMapLatest { user ->
            val userId = user?.remoteId ?: return@flatMapLatest flowOf(null)
            habitDao.getHabitById(habitId).map { habit ->
                habit?.takeIf { it.userId == userId && !it.isDeleted }?.toDomain()
            }
        }
    }

    fun observeAllCompletions(): Flow<List<HabitCompletion>> {
        return userDao.getLoggedUser().flatMapLatest { user ->
            val userId = user?.remoteId ?: return@flatMapLatest flowOf(emptyList())
            completionDao.observeCompletionsForUser(userId).map { completions ->
                completions.map { it.toDomain() }
            }
        }
    }

    fun observeCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>> {
        return userDao.getLoggedUser().flatMapLatest { user ->
            val userId = user?.remoteId ?: return@flatMapLatest flowOf(emptyList())
            completionDao.getCompletionsForHabit(habitId).map { completions ->
                completions
                    .filter { it.userId == userId && !it.isDeleted }
                    .map { it.toDomain() }
            }
        }
    }

    fun observeCompletionsByDate(date: String): Flow<List<HabitCompletion>> {
        return userDao.getLoggedUser().flatMapLatest { user ->
            val userId = user?.remoteId ?: return@flatMapLatest flowOf(emptyList())
            completionDao.getCompletionsByDate(userId, date).map { completions ->
                completions.map { it.toDomain() }
            }
        }
    }

    fun observeLocalCounts(): Flow<LocalDataCounts> {
        return userDao.getLoggedUser().flatMapLatest { user ->
            val userId = user?.remoteId ?: return@flatMapLatest flowOf(LocalDataCounts())
            combine(
                habitDao.countActiveHabitsForUser(userId),
                habitDao.countPendingHabitsForUser(userId),
                completionDao.countPendingCompletionsForUser(userId)
            ) { activeHabits, pendingHabits, pendingCompletions ->
                LocalDataCounts(
                    activeHabits = activeHabits,
                    unsyncedItems = pendingHabits + pendingCompletions
                )
            }
        }
    }

    suspend fun saveHabit(habit: Habit): Result<Long> {
        return runCatching {
            val userId = requireUserId()
            val now = System.currentTimeMillis()
            val baseCreatedAt = if (habit.id == 0L) now else habit.createdAt
            val entity = habit.copy(
                userId = userId,
                updatedAt = now,
                createdAt = baseCreatedAt,
                pendingSync = true
            ).toEntity(pendingSync = true)

            val localId = if (habit.id == 0L) {
                habitDao.insertHabit(entity)
            } else {
                habitDao.updateHabit(entity)
                habit.id
            }

            val saved = habitDao.getHabitByIdOnce(localId)
            if (saved?.reminderEnabled == true) {
                val hour = saved.reminderHour ?: saved.reminderTime?.substringBefore(":")?.toIntOrNull()
                val minute = saved.reminderMinute ?: saved.reminderTime?.substringAfter(":")?.toIntOrNull()
                if (hour != null && minute != null) {
                    reminderScheduler.schedule(localId, saved.title, hour, minute)
                }
            } else {
                reminderScheduler.cancel(localId)
            }
            localId
        }
    }

    suspend fun setCompletion(
        habit: Habit,
        date: String,
        value: Double,
        completed: Boolean
    ): Result<Unit> {
        return runCatching {
            val userId = requireUserId()
            val now = System.currentTimeMillis()
            val existing = completionDao.getCompletionForHabitOnDate(habit.id, userId, date)
            val completion = HabitCompletionEntity(
                id = existing?.id ?: 0,
                remoteId = existing?.remoteId,
                habitId = habit.id,
                userId = userId,
                date = date,
                value = value.coerceAtLeast(0.0),
                completed = completed,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
                pendingSync = true
            )
            completionDao.insertOrUpdateCompletion(completion)
        }
    }

    suspend fun deleteHabit(habitId: Long): Result<Unit> {
        return runCatching {
            habitDao.softDeleteHabit(habitId)
            reminderScheduler.cancel(habitId)
        }
    }

    private suspend fun requireUserId(): String {
        return userDao.getLoggedUserOnce()?.remoteId
            ?: error("Effettua il login prima di modificare le routine.")
    }
}

data class LocalDataCounts(
    val activeHabits: Int = 0,
    val unsyncedItems: Int = 0
)
