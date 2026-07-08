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
    val resetCode: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val isResetCodeSent: Boolean = false,
    val isPasswordResetComplete: Boolean = false,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateDisplayName(value: String) {
        _uiState.update { it.copy(displayName = value, errorMessage = null, successMessage = null) }
    }

    fun updateEmail(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null, successMessage = null) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null, successMessage = null) }
    }

    fun updateConfirmPassword(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null, successMessage = null) }
    }

    fun updateResetCode(value: String) {
        val digits = value.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(resetCode = digits, errorMessage = null, successMessage = null) }
    }

    fun updateNewPassword(value: String) {
        _uiState.update { it.copy(newPassword = value, errorMessage = null, successMessage = null) }
    }

    fun updateConfirmNewPassword(value: String) {
        _uiState.update { it.copy(confirmNewPassword = value, errorMessage = null, successMessage = null) }
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

    fun requestPasswordReset() {
        val state = _uiState.value
        if (!state.email.isValidEmail()) {
            _uiState.update { it.copy(errorMessage = "Inserisci un indirizzo email valido.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = authRepository.requestPasswordReset(state.email)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isResetCodeSent = result.isSuccess,
                    errorMessage = result.exceptionOrNull()?.message,
                    successMessage = if (result.isSuccess) {
                        "Se l'email è registrata, riceverai un codice a 6 cifre."
                    } else {
                        null
                    }
                )
            }
        }
    }

    fun resetPassword() {
        val state = _uiState.value
        val error = when {
            !state.email.isValidEmail() -> "Inserisci un indirizzo email valido."
            state.resetCode.length != 6 -> "Inserisci il codice a 6 cifre."
            state.newPassword.length < 8 -> "La nuova password deve avere almeno 8 caratteri."
            state.newPassword != state.confirmNewPassword -> "Le password non coincidono."
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = authRepository.resetPassword(
                email = state.email,
                code = state.resetCode,
                newPassword = state.newPassword
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isPasswordResetComplete = result.isSuccess,
                    errorMessage = result.exceptionOrNull()?.message,
                    successMessage = if (result.isSuccess) {
                        "Password aggiornata. Ora puoi accedere."
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
