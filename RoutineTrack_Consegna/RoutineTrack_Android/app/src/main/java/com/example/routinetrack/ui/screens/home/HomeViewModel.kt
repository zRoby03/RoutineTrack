package com.example.routinetrack.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.routinetrack.data.repository.HabitRepository
import com.example.routinetrack.domain.model.Habit
import com.example.routinetrack.domain.model.HabitCompletion
import com.example.routinetrack.domain.model.HabitType
import com.example.routinetrack.domain.model.isActiveOn
import com.example.routinetrack.domain.usecase.CalculateCurrentStreakUseCase
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max

data class HabitTodayItem(
    val habit: Habit,
    val progressValue: Double,
    val completed: Boolean,
    val currentStreak: Int
)

data class HomeUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val items: List<HabitTodayItem> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val habitRepository: HabitRepository,
    private val calculateCurrentStreak: CalculateCurrentStreakUseCase = CalculateCurrentStreakUseCase()
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val transientError = MutableStateFlow<String?>(null)

    // StateFlow espone uno stato già pronto per Compose.
    // Le coroutine partono nel viewModelScope, quindi non bloccano il Main Thread.
    val uiState: StateFlow<HomeUiState> = combine(
        selectedDate,
        habitRepository.observeHabits(),
        habitRepository.observeAllCompletions(),
        transientError
    ) { date, habits, completions, error ->
        val items = habits
            .filter { it.frequency.includes(date.dayOfWeek) && it.isActiveOn(date) }
            .map { habit ->
                val completion = completions.firstOrNull {
                    it.habitId == habit.id && it.date == date.toString()
                }
                HabitTodayItem(
                    habit = habit,
                    progressValue = completion?.value ?: 0.0,
                    completed = completion?.completed == true,
                    currentStreak = currentStreakFor(habit, completions, date)
                )
            }
        HomeUiState(selectedDate = date, items = items, errorMessage = error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun previousWeek() {
        selectedDate.value = selectedDate.value.minusWeeks(1)
    }

    fun nextWeek() {
        selectedDate.value = selectedDate.value.plusWeeks(1)
    }

    fun toggleComplete(item: HabitTodayItem) {
        val targetCompleted = !item.completed
        val value = when {
            item.habit.type == HabitType.NUMERIC && targetCompleted -> item.habit.targetValue ?: item.progressValue
            targetCompleted -> 1.0
            else -> 0.0
        }
        saveCompletion(item.habit, value, targetCompleted)
    }

    fun incrementProgress(item: HabitTodayItem) {
        val target = item.habit.targetValue ?: 1.0
        val value = item.progressValue + progressStep()
        saveCompletion(item.habit, value, value >= target)
    }

    fun decrementProgress(item: HabitTodayItem) {
        val target = item.habit.targetValue ?: 1.0
        val value = max(0.0, item.progressValue - progressStep())
        saveCompletion(item.habit, value, value >= target)
    }

    fun setManualProgress(item: HabitTodayItem, value: Double) {
        val target = item.habit.targetValue ?: 1.0
        val safeValue = max(0.0, value)
        saveCompletion(item.habit, safeValue, safeValue >= target)
    }

    private fun saveCompletion(habit: Habit, value: Double, completed: Boolean) {
        viewModelScope.launch {
            val result = habitRepository.setCompletion(
                habit = habit,
                date = selectedDate.value.toString(),
                value = value,
                completed = completed
            )
            transientError.value = result.exceptionOrNull()?.message
        }
    }

    private fun currentStreakFor(
        habit: Habit,
        completions: List<HabitCompletion>,
        referenceDate: LocalDate
    ): Int {
        val dates = completions
            .filter { it.habitId == habit.id && it.completed }
            .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
            .toSet()
        return calculateCurrentStreak(dates, referenceDate)
    }

    private fun progressStep(): Double {
        return 1.0
    }

    companion object {
        fun factory(habitRepository: HabitRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(habitRepository) as T
                }
            }
        }
    }
}
