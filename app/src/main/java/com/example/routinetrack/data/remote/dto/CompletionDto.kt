package com.example.routinetrack.data.remote.dto

import com.squareup.moshi.Json

data class CompletionDto(
    val id: String? = null,
    @param:Json(name = "habit_id")
    val habitId: String,
    @param:Json(name = "local_habit_id")
    val localHabitId: Long? = null,
    @param:Json(name = "user_id")
    val userId: String,
    val date: String,
    val value: Double = 1.0,
    val completed: Boolean = true,
    @param:Json(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @param:Json(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @param:Json(name = "is_deleted")
    val isDeleted: Boolean = false
)
