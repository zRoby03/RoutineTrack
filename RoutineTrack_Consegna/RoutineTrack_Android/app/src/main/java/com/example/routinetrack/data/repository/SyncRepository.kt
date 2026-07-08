package com.example.routinetrack.data.repository

import com.example.routinetrack.data.local.dao.HabitCompletionDao
import com.example.routinetrack.data.local.dao.HabitDao
import com.example.routinetrack.data.local.dao.UserDao
import com.example.routinetrack.data.local.entity.HabitCompletionEntity
import com.example.routinetrack.data.local.entity.HabitEntity
import com.example.routinetrack.data.remote.ApiService
import com.example.routinetrack.data.remote.dto.CompletionSyncDto
import com.example.routinetrack.data.remote.dto.HabitSyncDto
import com.example.routinetrack.data.remote.dto.SyncRequestDto
import com.example.routinetrack.data.remote.dto.SyncResponseDto
import com.example.routinetrack.data.session.SessionManager
import com.example.routinetrack.domain.model.HabitCategory
import com.example.routinetrack.domain.model.HabitFrequency
import com.example.routinetrack.domain.model.HabitType
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class SyncRepository(
    private val apiService: ApiService,
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {
    suspend fun syncNow(): Result<Unit> {
        return runCatching {
            val userId = currentUserId()
            val pendingHabits = habitDao.getPendingHabits(userId)
            val pendingCompletions = completionDao.getPendingCompletions(userId)
            val response = apiService.syncData(
                userId = userId,
                request = SyncRequestDto(
                    habits = pendingHabits.map { it.toSyncDto() },
                    completions = pendingCompletions.map { completion ->
                        val habit = habitDao.getHabitByIdOnce(completion.habitId)
                        completion.toSyncDto(habitRemoteId = habit?.remoteId)
                    }
                )
            )
            applyRemoteState(userId, response)
            sessionManager.saveLastSync(response.lastSync)
        }
    }

    suspend fun restoreFromCloud(): Result<Unit> {
        return runCatching {
            val userId = currentUserId()
            val response = apiService.getSyncData(userId)
            applyRemoteState(userId, response)
            sessionManager.saveLastSync(response.lastSync)
        }
    }

    fun observePendingCount(): Flow<Int> {
        return userDao.getLoggedUser().flatMapLatest { user ->
            val userId = user?.remoteId ?: return@flatMapLatest flowOf(0)
            combine(
                habitDao.countPendingHabitsForUser(userId),
                completionDao.countPendingCompletionsForUser(userId)
            ) { habits, completions -> habits + completions }
        }
    }

    fun getLastSync(): Long = sessionManager.getLastSync()

    private suspend fun currentUserId(): String {
        return userDao.getLoggedUserOnce()?.remoteId
            ?: error("Effettua il login prima di sincronizzare.")
    }

    private suspend fun applyRemoteState(userId: String, response: SyncResponseDto) {
        response.habits.forEach { dto ->
            val localId = dto.localId
                ?.takeIf { id -> habitDao.getHabitByIdOnce(id)?.userId == userId }
                ?: dto.remoteId?.let { habitDao.getHabitByRemoteId(it)?.id }
                ?: 0L

            if (localId == 0L) {
                val insertedId = habitDao.insertHabit(dto.toEntity(userId = userId, localId = 0L))
                habitDao.updateSyncState(insertedId, dto.remoteId, dto.updatedAt)
            } else {
                habitDao.updateHabit(dto.toEntity(userId = userId, localId = localId))
                habitDao.updateSyncState(localId, dto.remoteId, dto.updatedAt)
            }
        }

        response.completions.forEach { dto ->
            val habitLocalId = dto.habitLocalId
                ?.takeIf { id -> habitDao.getHabitByIdOnce(id)?.userId == userId }
                ?: dto.habitRemoteId?.let { habitDao.getHabitByRemoteId(it)?.id }
                ?: return@forEach
            val localId = dto.localId
                ?.takeIf { id -> completionDao.getCompletionByIdOnce(id)?.userId == userId }
                ?: dto.remoteId?.let { completionDao.getCompletionByRemoteId(it)?.id }
                ?: 0L

            if (localId == 0L) {
                val insertedId = completionDao.insertOrUpdateCompletion(
                    dto.toEntity(userId = userId, habitLocalId = habitLocalId, localId = 0L)
                )
                completionDao.updateSyncState(insertedId, dto.remoteId, dto.updatedAt)
            } else {
                completionDao.insertOrUpdateCompletion(
                    dto.toEntity(userId = userId, habitLocalId = habitLocalId, localId = localId)
                )
                completionDao.updateSyncState(localId, dto.remoteId, dto.updatedAt)
            }
        }
    }
}

private fun HabitEntity.toSyncDto(): HabitSyncDto {
    return HabitSyncDto(
        localId = id,
        remoteId = remoteId,
        userId = userId,
        title = title,
        description = description.ifBlank { null },
        emoji = category,
        color = color,
        targetValue = (targetValue ?: 1.0).toInt().coerceAtLeast(1),
        unit = unit,
        activeDays = frequency,
        reminderTime = reminderTime,
        startDate = startDate,
        endDate = endDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )
}

private fun CompletionSyncDto.toEntity(userId: String, habitLocalId: Long, localId: Long): HabitCompletionEntity {
    return HabitCompletionEntity(
        id = localId,
        remoteId = remoteId,
        habitId = habitLocalId,
        userId = userId,
        date = date,
        value = value.toDouble(),
        completed = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        pendingSync = false
    )
}

private fun HabitSyncDto.toEntity(userId: String, localId: Long): HabitEntity {
    val parsedCategory = runCatching { HabitCategory.valueOf(emoji ?: HabitCategory.OTHER.name) }
        .getOrDefault(HabitCategory.OTHER)
    val type = if (unit.isNullOrBlank() && targetValue <= 1) HabitType.BOOLEAN else HabitType.NUMERIC

    return HabitEntity(
        id = localId,
        remoteId = remoteId,
        userId = userId,
        title = title,
        description = description.orEmpty(),
        category = parsedCategory.name,
        color = color ?: "#C8B88A",
        type = type.name,
        targetValue = if (type == HabitType.NUMERIC) targetValue.toDouble() else null,
        unit = unit,
        frequency = activeDays ?: HabitFrequency.daily().toStorage(),
        reminderEnabled = !reminderTime.isNullOrBlank(),
        reminderTime = reminderTime,
        reminderHour = reminderTime?.substringBefore(":")?.toIntOrNull(),
        reminderMinute = reminderTime?.substringAfter(":")?.toIntOrNull(),
        startDate = startDate ?: LocalDate.now().toString(),
        endDate = endDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        pendingSync = false
    )
}

private fun HabitCompletionEntity.toSyncDto(habitRemoteId: String?): CompletionSyncDto {
    return CompletionSyncDto(
        localId = id,
        remoteId = remoteId,
        userId = userId,
        habitLocalId = habitId,
        habitRemoteId = habitRemoteId,
        date = date,
        value = value.toInt().coerceAtLeast(0),
        isCompleted = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )
}
