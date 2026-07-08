package com.example.routinetrack.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PasswordResetScreen(
    viewModel: AuthViewModel,
    onLoginClick: () -> Unit
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val buttonText = when {
        state.isLoading && !state.isResetCodeSent -> "Invio codice..."
        state.isLoading -> "Aggiorno password..."
        state.isPasswordResetComplete -> "Password aggiornata"
        state.isResetCodeSent -> "Aggiorna password"
        else -> "Invia codice"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Recupera password",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Inserisci la tua email e ti invieremo un codice di verifica.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.email,
            onValueChange = viewModel::updateEmail,
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !state.isLoading && !state.isPasswordResetComplete,
            singleLine = true
        )

        if (state.isResetCodeSent) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.resetCode,
                onValueChange = viewModel::updateResetCode,
                label = { Text("Codice a 6 cifre") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                enabled = !state.isLoading && !state.isPasswordResetComplete,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.newPassword,
                onValueChange = viewModel::updateNewPassword,
                label = { Text("Nuova password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !state.isLoading && !state.isPasswordResetComplete,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.confirmNewPassword,
                onValueChange = viewModel::updateConfirmNewPassword,
                label = { Text("Conferma nuova password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !state.isLoading && !state.isPasswordResetComplete,
                singleLine = true
            )
        }

        state.errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        state.successMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.secondary)
        }

        Spacer(modifier = Modifier.height(22.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && !state.isPasswordResetComplete,
            onClick = {
                if (state.isResetCodeSent) {
                    viewModel.resetPassword()
                } else {
                    viewModel.requestPasswordReset()
                }
            }
        ) {
            Text(buttonText)
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onLoginClick
        ) {
            Text(if (state.isPasswordResetComplete) "Vai al login" else "Torna al login")
        }
    }
}
