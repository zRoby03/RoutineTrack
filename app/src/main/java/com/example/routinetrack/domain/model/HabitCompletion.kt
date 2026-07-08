package com.example.routinetrack.domain.model

data class HabitCompletion(
    val id: Long = 0,
    val remoteId: String? = null,
    val habitId: Long,
    val userId: String = "",
    val date: String,
    val value: Double = 1.0,
    val completed: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val pendingSync: Boolean = true
)
