package com.example.routinetrack.data.remote.dto

data class HabitSyncDto(
    val localId: Long?,
    val remoteId: String?,
    val userId: String,
    val title: String,
    val description: String?,
    val emoji: String?,
    val color: String?,
    val targetValue: Int,
    val unit: String?,
    val activeDays: String?,
    val reminderTime: String?,
    val startDate: String? = null,
    val endDate: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean
)

data class CompletionSyncDto(
    val localId: Long?,
    val remoteId: String?,
    val userId: String,
    val habitLocalId: Long?,
    val habitRemoteId: String?,
    val date: String,
    val value: Int,
    val isCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean
)

data class SyncRequestDto(
    val habits: List<HabitSyncDto>,
    val completions: List<CompletionSyncDto>
)

data class SyncResponseDto(
    val habits: List<HabitSyncDto>,
    val completions: List<CompletionSyncDto>,
    val lastSync: Long
)
