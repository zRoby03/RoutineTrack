package com.example.routinetrack.data.session

data class UserSession(
    val userId: String,
    val email: String,
    val displayName: String?,
    val token: String? = null
)
