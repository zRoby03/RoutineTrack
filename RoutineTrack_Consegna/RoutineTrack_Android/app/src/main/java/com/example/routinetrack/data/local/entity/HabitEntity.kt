package com.example.routinetrack.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "remote_id")
    val remoteId: String? = null,
    @ColumnInfo(name = "user_id")
    val userId: String = "",
    val title: String,
    val description: String,
    val category: String,
    val color: String,
    val type: String,
    val targetValue: Double? = null,
    val unit: String? = null,
    val frequency: String,
    val reminderEnabled: Boolean = false,
    val reminderTime: String? = null,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    @ColumnInfo(name = "start_date")
    val startDate: String = LocalDate.now().toString(),
    @ColumnInfo(name = "end_date")
    val endDate: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "pending_sync")
    val pendingSync: Boolean = true
)
