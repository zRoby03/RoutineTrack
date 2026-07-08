package com.example.routinetrack.data.repository

import com.example.routinetrack.data.local.dao.UserDao
import com.example.routinetrack.data.local.entity.UserEntity
import com.example.routinetrack.data.mapper.toDomain
import com.example.routinetrack.data.remote.ApiService
import com.example.routinetrack.data.remote.dto.LoginRequestDto
import com.example.routinetrack.data.remote.dto.RegisterRequestDto
import com.example.routinetrack.data.session.SessionManager
import com.example.routinetrack.data.session.UserSession
import com.example.routinetrack.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {
    val loggedUser: Flow<User?> = userDao.getLoggedUser()
        .map { it?.toDomain() }

    suspend fun login(email: String, password: String): Result<User> {
        return runCatching {
            val response = apiService.login(LoginRequestDto(email.trim(), password))
            val user = UserEntity(
                id = 0,
                remoteId = response.userId,
                email = response.email,
                displayName = response.displayName,
                token = response.token,
                isLoggedIn = true
            )
            userDao.clearUser()
            userDao.insertUser(user)
            sessionManager.saveSession(
                UserSession(
                    userId = response.userId,
                    email = response.email,
                    displayName = response.displayName,
                    token = response.token
                )
            )
            user.toDomain()
        }
    }

    suspend fun register(displayName: String?, email: String, password: String): Result<User> {
        return runCatching {
            val response = apiService.register(
                RegisterRequestDto(
                    email = email.trim(),
                    password = password,
                    displayName = displayName?.trim()?.takeIf { it.isNotBlank() }
                )
            )
            val user = UserEntity(
                id = 0,
                remoteId = response.userId,
                email = response.email,
                displayName = response.displayName,
                token = response.token,
                isLoggedIn = true
            )
            userDao.clearUser()
            userDao.insertUser(user)
            sessionManager.saveSession(
                UserSession(
                    userId = response.userId,
                    email = response.email,
                    displayName = response.displayName,
                    token = response.token
                )
            )
            user.toDomain()
        }
    }

    suspend fun logout() {
        userDao.clearUser()
        sessionManager.logout()
    }
}
