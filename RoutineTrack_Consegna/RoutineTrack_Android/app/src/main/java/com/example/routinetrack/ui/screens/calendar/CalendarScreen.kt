package com.example.routinetrack.ui.screens.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.routinetrack.ui.components.CompactHabitCard
import com.example.routinetrack.ui.components.EmptyState
import com.example.routinetrack.ui.components.MonthlyCalendar
import com.example.routinetrack.ui.components.RoutineTopBar
import com.example.routinetrack.ui.components.StatCard
import com.example.routinetrack.ui.theme.PastelBlue
import com.example.routinetrack.ui.theme.PastelGreen
import com.example.routinetrack.ui.theme.PastelPurple
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onHabitClick: (Long) -> Unit
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val monthTitle = "${state.month.month.getDisplayName(TextStyle.FULL, Locale.ITALIAN)} ${state.month.year}"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 18.dp)
    ) {
        item {
            RoutineTopBar(
                title = "Calendario",
                subtitle = "Report mensile",
                message = "I giorni colorati indicano almeno un completamento."
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Mese precedente")
                }
                Text(monthTitle.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold)
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Mese successivo")
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.selectedHabitId == null,
                    onClick = { viewModel.selectHabit(null) },
                    label = { Text("Tutte") }
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.habits.take(3).forEach { habit ->
                    FilterChip(
                        selected = state.selectedHabitId == habit.id,
                        onClick = { viewModel.selectHabit(habit.id) },
                        label = { Text(habit.title.take(12)) }
                    )
                }
            }
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                MonthlyCalendar(
                    month = state.month,
                    highlightedDates = state.highlightedDates,
                    selectedDate = state.selectedDate,
                    onDateClick = viewModel::selectDate,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Completamenti",
                    value = state.monthlyCompletions.toString(),
                    subtitle = "nel mese",
                    color = PastelBlue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Giorni attivi",
                    value = state.activeDays.toString(),
                    subtitle = "con habit fatte",
                    color = PastelGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            StatCard(
                title = "Percentuale mese",
                value = "${state.monthlyRate}%",
                subtitle = "sul piano previsto",
                color = PastelPurple
            )
        }
        item {
            Text(
                text = "Completamenti del ${state.selectedDate.dayOfMonth}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        if (state.selectedCompletions.isEmpty()) {
            item {
                EmptyState(
                    title = "Nessun completamento",
                    message = "Quando completi una habit comparira qui."
                )
            }
        } else {
            items(state.selectedCompletions) { completion ->
                CompactHabitCard(
                    title = completion.habitTitle,
                    subtitle = completion.valueText,
                    color = completion.color,
                    onClick = {
                        val habit = state.habits.firstOrNull { it.title == completion.habitTitle }
                        if (habit != null) onHabitClick(habit.id)
                    }
                )
            }
        }
    }
}
