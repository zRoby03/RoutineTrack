package com.example.routinetrack.data.mapper

import com.example.routinetrack.data.local.entity.HabitCompletionEntity
import com.example.routinetrack.data.remote.dto.CompletionDto
import com.example.routinetrack.domain.model.HabitCompletion

fun HabitCompletionEntity.toDomain(): HabitCompletion {
    return HabitCompletion(
        id = id,
        remoteId = remoteId,
        habitId = habitId,
        userId = userId,
        date = date,
        value = value,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        pendingSync = pendingSync
    )
}

fun HabitCompletion.toEntity(pendingSync: Boolean = this.pendingSync): HabitCompletionEntity {
    return HabitCompletionEntity(
        id = id,
        remoteId = remoteId,
        habitId = habitId,
        userId = userId,
        date = date,
        value = value,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        pendingSync = pendingSync
    )
}

fun HabitCompletionEntity.toDto(remoteHabitId: String? = null): CompletionDto {
    return CompletionDto(
        id = remoteId,
        habitId = remoteHabitId ?: habitId.toString(),
        localHabitId = habitId,
        userId = userId,
        date = date,
        value = value,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )
}
