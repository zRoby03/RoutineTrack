package com.example.routinetrack.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.routinetrack.data.remote.RetrofitClient
import com.example.routinetrack.data.repository.AuthRepository
import com.example.routinetrack.data.repository.HabitRepository
import com.example.routinetrack.data.repository.LocalDataCounts
import com.example.routinetrack.data.repository.SyncRepository
import com.example.routinetrack.domain.model.User
import com.example.routinetrack.sync.SyncManager
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val user: User? = null,
    val counts: LocalDataCounts = LocalDataCounts(),
    val isSyncing: Boolean = false,
    val message: String? = null,
    val baseUrl: String = RetrofitClient.DEFAULT_BASE_URL,
    val lastSyncLabel: String = "Mai"
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    habitRepository: HabitRepository,
    private val syncRepository: SyncRepository,
    private val syncManager: SyncManager
) : ViewModel() {
    private val syncing = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        authRepository.loggedUser,
        habitRepository.observeLocalCounts(),
        syncing,
        message
    ) { user, counts, isSyncing, currentMessage ->
        SettingsUiState(
            user = user,
            counts = counts,
            isSyncing = isSyncing,
            message = currentMessage,
            lastSyncLabel = formatLastSync(syncRepository.getLastSync())
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun syncNow() {
        viewModelScope.launch {
            syncing.value = true
            val result = syncManager.syncNow()
            syncing.value = false
            message.value = if (result.isSuccess) {
                "Sincronizzazione completata"
            } else {
                "Sincronizzazione non riuscita. Riprova più tardi."
            }
        }
    }

    fun restoreFromCloud() {
        viewModelScope.launch {
            syncing.value = true
            val result = syncManager.restoreFromCloud()
            syncing.value = false
            message.value = if (result.isSuccess) {
                "Dati cloud ripristinati"
            } else {
                "Ripristino non riuscito. Riprova più tardi."
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }

    companion object {
        fun factory(
            authRepository: AuthRepository,
            habitRepository: HabitRepository,
            syncRepository: SyncRepository,
            syncManager: SyncManager
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(
                        authRepository = authRepository,
                        habitRepository = habitRepository,
                        syncRepository = syncRepository,
                        syncManager = syncManager
                    ) as T
                }
            }
        }
    }
}

private fun formatLastSync(timestamp: Long): String {
    if (timestamp <= 0L) return "Mai"
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}
