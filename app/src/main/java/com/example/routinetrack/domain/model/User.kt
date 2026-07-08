package com.example.routinetrack.domain.model

data class User(
    val id: Long = 0,
    val remoteId: String? = null,
    val email: String,
    val displayName: String?,
    val token: String? = null,
    val isLoggedIn: Boolean = true
)
