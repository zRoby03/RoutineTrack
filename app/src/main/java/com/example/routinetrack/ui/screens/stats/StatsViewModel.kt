package com.example.routinetrack.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.routinetrack.data.repository.StatsRepository
import com.example.routinetrack.domain.model.HabitCategory
import com.example.routinetrack.domain.model.HabitStats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class StatsUiState(
    val selectedCategory: HabitCategory? = null,
    val stats: HabitStats = HabitStats()
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    private val statsRepository: StatsRepository
) : ViewModel() {
    private val selectedCategory = MutableStateFlow<HabitCategory?>(null)

    val uiState: StateFlow<StatsUiState> = selectedCategory
        .flatMapLatest { category ->
            statsRepository.observeStatsForCategory(category)
                .map { StatsUiState(selectedCategory = category, stats = it) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsUiState()
        )

    fun selectCategory(category: HabitCategory?) {
        selectedCategory.update { category }
    }

    companion object {
        fun factory(statsRepository: StatsRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StatsViewModel(statsRepository) as T
                }
            }
        }
    }
}
