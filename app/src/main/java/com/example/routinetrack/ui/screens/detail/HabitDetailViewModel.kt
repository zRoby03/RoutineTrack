package com.example.routinetrack.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.routinetrack.data.repository.HabitRepository
import com.example.routinetrack.data.repository.StatsRepository
import com.example.routinetrack.domain.model.Habit
import com.example.routinetrack.domain.model.HabitCompletion
import com.example.routinetrack.domain.model.HabitStats
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HabitDetailUiState(
    val habit: Habit? = null,
    val completions: List<HabitCompletion> = emptyList(),
    val stats: HabitStats = HabitStats(),
    val todayCompletion: HabitCompletion? = null,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)

class HabitDetailViewModel(
    private val habitId: Long,
    private val habitRepository: HabitRepository,
    statsRepository: StatsRepository
) : ViewModel() {
    val uiState: StateFlow<HabitDetailUiState> = combine(
        habitRepository.observeHabit(habitId),
        habitRepository.observeCompletionsForHabit(habitId),
        statsRepository.observeStatsForHabit(habitId)
    ) { habit, completions, stats ->
        HabitDetailUiState(
            habit = habit,
            completions = completions,
            stats = stats,
            todayCompletion = completions.firstOrNull { it.date == LocalDate.now().toString() }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HabitDetailUiState()
    )

    fun deleteHabit(onDeleted: () -> Unit) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habitId)
            onDeleted()
        }
    }

    companion object {
        fun factory(
            habitId: Long,
            habitRepository: HabitRepository,
            statsRepository: StatsRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HabitDetailViewModel(habitId, habitRepository, statsRepository) as T
                }
            }
        }
    }
}
