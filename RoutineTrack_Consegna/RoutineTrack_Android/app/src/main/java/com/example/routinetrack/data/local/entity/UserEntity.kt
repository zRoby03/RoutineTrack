package com.example.routinetrack.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Long = 0,
    @ColumnInfo(name = "remote_id")
    val remoteId: String? = null,
    val email: String,
    @ColumnInfo(name = "display_name")
    val displayName: String?,
    val token: String? = null,
    @ColumnInfo(name = "is_logged_in")
    val isLoggedIn: Boolean = true
)
