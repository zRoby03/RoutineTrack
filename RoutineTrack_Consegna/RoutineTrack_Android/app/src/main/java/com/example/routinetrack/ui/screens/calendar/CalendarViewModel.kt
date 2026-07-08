package com.example.routinetrack.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.routinetrack.data.repository.HabitRepository
import com.example.routinetrack.domain.model.Habit
import com.example.routinetrack.domain.model.isActiveDuring
import com.example.routinetrack.domain.model.isActiveOn
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CalendarDayCompletion(
    val habitTitle: String,
    val valueText: String,
    val color: String
)

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedHabitId: Long? = null,
    val habits: List<Habit> = emptyList(),
    val highlightedDates: Set<LocalDate> = emptySet(),
    val selectedCompletions: List<CalendarDayCompletion> = emptyList(),
    val monthlyCompletions: Int = 0,
    val activeDays: Int = 0,
    val monthlyRate: Int = 0
)

class CalendarViewModel(
    habitRepository: HabitRepository
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val selectedHabitId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<CalendarUiState> = combine(
        month,
        selectedDate,
        selectedHabitId,
        habitRepository.observeHabits(),
        habitRepository.observeAllCompletions()
    ) { currentMonth, selected, filterHabitId, habits, completions ->
        val visibleHabits = habits.filter { it.isActiveDuring(currentMonth) }
        val habitById = visibleHabits.associateBy { it.id }
        val filtered = completions.filter { completion ->
            val date = runCatching { LocalDate.parse(completion.date) }.getOrNull()
            val habit = habitById[completion.habitId]
            completion.completed &&
                date != null &&
                habit != null &&
                habit.isActiveOn(date) &&
                YearMonth.from(date) == currentMonth &&
                (filterHabitId == null || completion.habitId == filterHabitId)
        }
        val highlightedDates = filtered.mapNotNull {
            runCatching { LocalDate.parse(it.date) }.getOrNull()
        }.toSet()
        val expectedDays = visibleHabits.sumOf { habit ->
            (1..currentMonth.lengthOfMonth()).count { day ->
                val date = currentMonth.atDay(day)
                habit.frequency.includes(date.dayOfWeek) && habit.isActiveOn(date)
            }
        }
        val completedKeys = filtered.map { "${it.habitId}|${it.date}" }.toSet()
        CalendarUiState(
            month = currentMonth,
            selectedDate = selected,
            selectedHabitId = filterHabitId,
            habits = visibleHabits,
            highlightedDates = highlightedDates,
            selectedCompletions = filtered
                .filter { it.date == selected.toString() }
                .mapNotNull { completion ->
                    val habit = habitById[completion.habitId] ?: return@mapNotNull null
                    CalendarDayCompletion(
                        habitTitle = habit.title,
                        valueText = if (habit.targetValue != null) {
                            "${completion.value.toInt()} ${habit.unit.orEmpty()}".trim()
                        } else {
                            "Completata"
                        },
                        color = habit.color
                    )
                },
            monthlyCompletions = filtered.size,
            activeDays = highlightedDates.size,
            monthlyRate = if (expectedDays == 0) {
                0
            } else {
                ((completedKeys.size.toDouble() / expectedDays.toDouble()) * 100).toInt().coerceIn(0, 100)
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState()
    )

    fun previousMonth() {
        month.value = month.value.minusMonths(1)
    }

    fun nextMonth() {
        month.value = month.value.plusMonths(1)
    }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun selectHabit(habitId: Long?) {
        selectedHabitId.value = habitId
    }

    companion object {
        fun factory(habitRepository: HabitRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CalendarViewModel(habitRepository) as T
                }
            }
        }
    }
}
