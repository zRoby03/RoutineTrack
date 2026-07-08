package com.example.routinetrack.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.routinetrack.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateDisplayName(value: String) {
        _uiState.update { it.copy(displayName = value, errorMessage = null) }
    }

    fun updateEmail(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun updateConfirmPassword(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Inserisci email e password.") }
            return
        }
        if (!state.email.isValidEmail()) {
            _uiState.update { it.copy(errorMessage = "Inserisci un indirizzo email valido.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(state.email, state.password)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isLoggedIn = result.isSuccess,
                    errorMessage = result.exceptionOrNull()?.message ?: if (result.isFailure) {
                        "Accesso non riuscito. Controlla i dati e riprova."
                    } else {
                        null
                    }
                )
            }
        }
    }

    fun register() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Inserisci email e password.") }
            return
        }
        if (!state.email.isValidEmail()) {
            _uiState.update { it.copy(errorMessage = "Inserisci un indirizzo email valido.") }
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Le password non coincidono.") }
            return
        }
        if (state.password.length < 8) {
            _uiState.update { it.copy(errorMessage = "La password deve avere almeno 8 caratteri.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.register(state.displayName, state.email, state.password)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isLoggedIn = result.isSuccess,
                    errorMessage = result.exceptionOrNull()?.message ?: if (result.isFailure) {
                        "Registrazione non riuscita. Riprova più tardi."
                    } else {
                        null
                    }
                )
            }
        }
    }

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(authRepository) as T
                }
            }
        }
    }
}

private fun String.isValidEmail(): Boolean {
    val clean = trim()
    val domain = clean.substringAfter("@", missingDelimiterValue = "")
    return clean.contains("@") && "." in domain && clean.length >= 6
}
