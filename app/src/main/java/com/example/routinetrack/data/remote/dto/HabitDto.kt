package com.example.routinetrack.data.remote.dto

import com.squareup.moshi.Json

data class HabitDto(
    val id: String? = null,
    @Json(name = "user_id")
    val userId: String,
    val title: String,
    val description: String,
    val category: String,
    val color: String,
    val type: String,
    @Json(name = "target_value")
    val targetValue: Double? = null,
    val unit: String? = null,
    val frequency: String,
    @Json(name = "reminder_enabled")
    val reminderEnabled: Boolean = false,
    @Json(name = "reminder_time")
    val reminderTime: String? = null,
    @Json(name = "start_date")
    val startDate: String? = null,
    @Json(name = "end_date")
    val endDate: String? = null,
    @Json(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @Json(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @Json(name = "is_deleted")
    val isDeleted: Boolean = false
)
