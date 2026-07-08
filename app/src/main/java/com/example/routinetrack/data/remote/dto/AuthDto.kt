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

data class PasswordResetRequestDto(
    val email: String
)

data class PasswordResetConfirmDto(
    val email: String,
    val code: String,
    val newPassword: String
)

data class MessageResponseDto(
    val message: String? = null
)

data class AuthResponseDto(
    val userId: String,
    val email: String,
    val displayName: String?,
    val token: String? = null
)
