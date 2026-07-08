package com.example.routinetrack.data.mapper

import com.example.routinetrack.data.local.entity.HabitEntity
import com.example.routinetrack.data.remote.dto.HabitDto
import com.example.routinetrack.domain.model.Habit
import com.example.routinetrack.domain.model.HabitCategory
import com.example.routinetrack.domain.model.HabitFrequency
import com.example.routinetrack.domain.model.HabitType

fun HabitEntity.toDomain(): Habit {
    return Habit(
        id = id,
        remoteId = remoteId,
        userId = userId,
        title = title,
        description = description,
        category = runCatching { HabitCategory.valueOf(category) }.getOrDefault(HabitCategory.OTHER),
        color = color,
        type = runCatching { HabitType.valueOf(type) }.getOrDefault(HabitType.BOOLEAN),
        targetValue = targetValue,
        unit = unit,
        frequency = HabitFrequency.fromStorage(frequency),
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime,
        reminderHour = reminderHour,
        reminderMinute = reminderMinute,
        startDate = startDate,
        endDate = endDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        pendingSync = pendingSync
    )
}

fun Habit.toEntity(pendingSync: Boolean = this.pendingSync): HabitEntity {
    return HabitEntity(
        id = id,
        remoteId = remoteId,
        userId = userId,
        title = title,
        description = description,
        category = category.name,
        color = color,
        type = type.name,
        targetValue = targetValue,
        unit = unit,
        frequency = frequency.toStorage(),
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime,
        reminderHour = reminderHour,
        reminderMinute = reminderMinute,
        startDate = startDate,
        endDate = endDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        pendingSync = pendingSync
    )
}

fun HabitEntity.toDto(): HabitDto {
    return HabitDto(
        id = remoteId,
        userId = userId,
        title = title,
        description = description,
        category = category,
        color = color,
        type = type,
        targetValue = targetValue,
        unit = unit,
        frequency = frequency,
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime,
        startDate = startDate,
        endDate = endDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )
}

fun HabitDto.toEntity(localId: Long = 0, pendingSync: Boolean = false): HabitEntity {
    return HabitEntity(
        id = localId,
        remoteId = id,
        userId = userId,
        title = title,
        description = description,
        category = category,
        color = color,
        type = type,
        targetValue = targetValue,
        unit = unit,
        frequency = frequency,
        reminderEnabled = reminderEnabled,
        reminderTime = reminderTime,
        reminderHour = reminderTime?.substringBefore(":")?.toIntOrNull(),
        reminderMinute = reminderTime?.substringAfter(":")?.toIntOrNull(),
        startDate = startDate ?: java.time.LocalDate.now().toString(),
        endDate = endDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        pendingSync = pendingSync
    )
}
