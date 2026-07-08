package com.example.routinetrack.ui.screens.addhabit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.routinetrack.data.repository.HabitRepository
import com.example.routinetrack.domain.model.FrequencyMode
import com.example.routinetrack.domain.model.Habit
import com.example.routinetrack.domain.model.HabitCategory
import com.example.routinetrack.domain.model.HabitFrequency
import com.example.routinetrack.domain.model.HabitType
import com.example.routinetrack.domain.model.HabitUnit
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddHabitUiState(
    val editingHabitId: Long? = null,
    val title: String = "",
    val description: String = "",
    val category: HabitCategory = HabitCategory.HEALTH,
    val color: String = "#FADADD",
    val type: HabitType = HabitType.BOOLEAN,
    val targetValue: String = "1",
    val unit: HabitUnit = HabitUnit.TIMES,
    val frequencyMode: FrequencyMode = FrequencyMode.DAILY,
    val selectedDays: Set<Int> = HabitFrequency.allDays,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedHabitId: Long? = null
)

class AddHabitViewModel(
    private val habitRepository: HabitRepository,
    private val habitId: Long?
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddHabitUiState(editingHabitId = habitId))
    val uiState: StateFlow<AddHabitUiState> = _uiState.asStateFlow()
    private var originalHabit: Habit? = null
    private var loaded = false

    init {
        if (habitId != null && habitId > 0) {
            viewModelScope.launch {
                habitRepository.observeHabit(habitId).collect { habit ->
                    if (habit != null && !loaded) {
                        originalHabit = habit
                        loaded = true
                        _uiState.update {
                            it.copy(
                                title = habit.title,
                                description = habit.description,
                                category = habit.category,
                                color = habit.color,
                                type = habit.type,
                                unit = HabitUnit.fromLabel(habit.unit),
                                targetValue = if (HabitUnit.fromLabel(habit.unit) == HabitUnit.TIME) {
                                    secondsToDurationText(habit.targetValue?.toInt() ?: 0)
                                } else {
                                    habit.targetValue?.toInt()?.toString().orEmpty()
                                },
                                frequencyMode = habit.frequency.mode,
                                selectedDays = habit.frequency.daysOfWeek,
                                reminderEnabled = habit.reminderEnabled,
                                reminderHour = habit.reminderHour
                                    ?: habit.reminderTime?.substringBefore(":")?.toIntOrNull()
                                    ?: 20,
                                reminderMinute = habit.reminderMinute
                                    ?: habit.reminderTime?.substringAfter(":")?.toIntOrNull()
                                    ?: 0,
                                startDate = parseDateOrDefault(habit.startDate, habit.createdAt),
                                endDate = habit.endDate?.let { endDate -> parseDateOrNull(endDate) }
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateTitle(value: String) = _uiState.update { it.copy(title = value, errorMessage = null) }
    fun updateDescription(value: String) = _uiState.update { it.copy(description = value) }
    fun updateCategory(value: HabitCategory) = _uiState.update { it.copy(category = value) }
    fun updateColor(value: String) = _uiState.update { it.copy(color = value) }
    fun updateType(value: HabitType) = _uiState.update { it.copy(type = value, errorMessage = null) }
    fun updateTargetValue(value: String) {
        _uiState.update { state ->
            val cleaned = if (state.unit == HabitUnit.TIME) {
                value.filter { it.isDigit() || it == ':' }.take(8)
            } else {
                value.filter { char -> char.isDigit() }
            }
            state.copy(targetValue = cleaned, errorMessage = null)
        }
    }
    fun updateUnit(value: HabitUnit) {
        _uiState.update { state ->
            state.copy(
                unit = value,
                type = HabitType.NUMERIC,
                targetValue = if (value == HabitUnit.TIME) {
                    state.targetValue.takeIf { isValidDurationText(it) } ?: "00:30:00"
                } else {
                    state.targetValue.toIntOrNull()?.takeIf { it > 0 }?.toString() ?: "1"
                },
                errorMessage = null
            )
        }
    }
    fun updateReminderEnabled(value: Boolean) = _uiState.update { it.copy(reminderEnabled = value) }
    fun updateReminderHour(value: Int) = _uiState.update { it.copy(reminderHour = value.coerceIn(0, 23), errorMessage = null) }
    fun updateReminderMinute(value: Int) = _uiState.update { it.copy(reminderMinute = value.coerceIn(0, 59), errorMessage = null) }
    fun updateStartDate(value: LocalDate) {
        _uiState.update { state ->
            state.copy(
                startDate = value,
                endDate = state.endDate?.takeIf { !it.isBefore(value) },
                errorMessage = null
            )
        }
    }
    fun updateEndDate(value: LocalDate?) = _uiState.update { it.copy(endDate = value, errorMessage = null) }

    fun updateFrequencyMode(value: FrequencyMode) {
        _uiState.update { it.copy(frequencyMode = value) }
    }

    fun toggleDay(day: Int) {
        _uiState.update { state ->
            val updatedDays = if (state.selectedDays.contains(day)) {
                state.selectedDays - day
            } else {
                state.selectedDays + day
            }
            state.copy(selectedDays = updatedDays.ifEmpty { setOf(day) })
        }
    }

    fun saveHabit() {
        val state = _uiState.value
        val target = if (state.unit == HabitUnit.TIME) {
            durationTextToSeconds(state.targetValue)?.toDouble()
        } else {
            state.targetValue.toDoubleOrNull()
        }

        val error = when {
            state.title.isBlank() -> "Il titolo e obbligatorio"
            state.type == HabitType.NUMERIC && (target == null || target <= 0.0) ->
                "Per una habit numerica serve un target maggiore di 0"
            state.unit == HabitUnit.TIME && !isValidDurationText(state.targetValue) ->
                "Inserisci una durata valida in formato hh:mm:ss"
            state.frequencyMode == FrequencyMode.SPECIFIC_DAYS && state.selectedDays.isEmpty() ->
                "Scegli almeno un giorno"
            state.endDate != null && state.endDate.isBefore(state.startDate) ->
                "La data finale deve essere uguale o successiva alla data iniziale"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val base = originalHabit
            val habit = Habit(
                id = base?.id ?: 0,
                remoteId = base?.remoteId,
                userId = base?.userId ?: "",
                title = state.title.trim(),
                description = state.description.trim(),
                category = state.category,
                color = state.color,
                type = state.type,
                targetValue = if (state.type == HabitType.NUMERIC) target else null,
                unit = if (state.type == HabitType.NUMERIC) state.unit.label else null,
                frequency = if (state.frequencyMode == FrequencyMode.DAILY) {
                    HabitFrequency.daily()
                } else {
                    HabitFrequency.specific(state.selectedDays)
                },
                reminderEnabled = state.reminderEnabled,
                reminderTime = if (state.reminderEnabled) {
                    "${state.reminderHour.twoDigits()}:${state.reminderMinute.twoDigits()}"
                } else {
                    null
                },
                reminderHour = if (state.reminderEnabled) state.reminderHour else null,
                reminderMinute = if (state.reminderEnabled) state.reminderMinute else null,
                startDate = state.startDate.toString(),
                endDate = state.endDate?.toString(),
                createdAt = base?.createdAt ?: System.currentTimeMillis()
            )

            val result = habitRepository.saveHabit(habit)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    savedHabitId = result.getOrNull(),
                    errorMessage = if (result.isFailure) {
                        result.exceptionOrNull()?.message ?: "Salvataggio non riuscito"
                    } else {
                        null
                    }
                )
            }
        }
    }

    companion object {
        fun factory(
            habitRepository: HabitRepository,
            habitId: Long?
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AddHabitViewModel(habitRepository, habitId) as T
                }
            }
        }
    }
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')

private fun parseDateOrNull(value: String): LocalDate? {
    return runCatching { LocalDate.parse(value) }.getOrNull()
}

private fun parseDateOrDefault(value: String, createdAt: Long): LocalDate {
    return parseDateOrNull(value)
        ?: Instant.ofEpochMilli(createdAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
}

private fun isValidDurationText(value: String): Boolean {
    return durationTextToSeconds(value) != null
}

private fun durationTextToSeconds(value: String): Int? {
    val parts = value.split(":")
    if (parts.size != 3) return null
    val hours = parts[0].toIntOrNull() ?: return null
    val minutes = parts[1].toIntOrNull() ?: return null
    val seconds = parts[2].toIntOrNull() ?: return null
    if (hours !in 0..99 || minutes !in 0..59 || seconds !in 0..59) return null
    val total = hours * 3600 + minutes * 60 + seconds
    return total.takeIf { it > 0 }
}

private fun secondsToDurationText(totalSeconds: Int): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0)
    val hours = safeSeconds / 3600
    val minutes = (safeSeconds % 3600) / 60
    val seconds = safeSeconds % 60
    return "${hours.twoDigits()}:${minutes.twoDigits()}:${seconds.twoDigits()}"
}
