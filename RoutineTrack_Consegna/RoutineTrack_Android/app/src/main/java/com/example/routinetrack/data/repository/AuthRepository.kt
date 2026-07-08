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
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

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
        }.mapAuthFailure(AuthAction.LOGIN)
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
        }.mapAuthFailure(AuthAction.REGISTER)
    }

    suspend fun logout() {
        userDao.clearUser()
        sessionManager.logout()
    }

    private fun <T> Result<T>.mapAuthFailure(action: AuthAction): Result<T> {
        return fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(IllegalStateException(it.toFriendlyAuthMessage(action), it)) }
        )
    }

    private fun Throwable.toFriendlyAuthMessage(action: AuthAction): String {
        return when {
            this is HttpException && code() == 401 && action == AuthAction.LOGIN ->
                "Email e/o password non corrette."
            this is HttpException && code() == 409 && action == AuthAction.REGISTER ->
                "Email già registrata. Prova ad accedere oppure usa un'altra email."
            this is HttpException && code() == 400 ->
                "Controlla i dati inseriti e riprova."
            this is HttpException ->
                "Operazione non riuscita. Riprova più tardi."
            this is IOException ->
                "Connessione non riuscita. Controlla la rete e riprova."
            else ->
                message ?: "Operazione account non riuscita."
        }
    }

    private enum class AuthAction {
        LOGIN,
        REGISTER
    }
}
