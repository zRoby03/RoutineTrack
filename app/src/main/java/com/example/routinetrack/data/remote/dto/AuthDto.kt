package com.example.routinetrack.data.remote.dto

import com.squareup.moshi.Json

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class RegisterRequestDto(
    val email: String,
    val password: String,
    val displayName: String?
)

data class AuthResponseDto(
    val userId: String,
    val email: String,
    val displayName: String?,
    val token: String? = null
)
