package com.example.routinetrack.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.routinetrack.domain.model.HabitCategory
import com.example.routinetrack.ui.components.CategoryChip
import com.example.routinetrack.ui.components.EmptyState
import com.example.routinetrack.ui.components.MonthlyCalendar
import com.example.routinetrack.ui.components.ProgressRingCard
import com.example.routinetrack.ui.components.RoutineTrackCard
import com.example.routinetrack.ui.components.SettingsRow
import com.example.routinetrack.ui.components.routineEmoji
import com.example.routinetrack.ui.theme.KhakiSoft
import com.example.routinetrack.ui.theme.RoutineTextPrimary
import com.example.routinetrack.ui.theme.RoutineTextSecondary
import com.example.routinetrack.ui.theme.SageSoft
import com.example.routinetrack.ui.theme.WarmBackground
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val stats = state.stats
    val month = YearMonth.now()
    val monthPrefix = month.toString()
    val highlightedDates = stats.annualHeatmap
        .filter { (date, count) -> date.startsWith(monthPrefix) && count > 0 }
        .keys
        .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
        .toSet()
    val perfectDays = highlightedDates.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 28.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = state.selectedCategory?.label ?: "Overall",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    CategoryChip(
                        label = "All",
                        icon = "★",
                        selected = state.selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) }
                    )
                }
                items(HabitCategory.entries) { category ->
                    CategoryChip(
                        label = category.label,
                        icon = category.routineEmoji(),
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) }
                    )
                }
            }
        }
        if (stats.totalCompletions == 0) {
            item {
                EmptyState(
                    title = "Statistiche non disponibili",
                    message = "Completa qualche abitudine per generare report."
                )
            }
        }
        item {
            RoutineTrackCard(cornerRadius = 32.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = monthLabel(month),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${stats.totalCompletions} done",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                MonthlyCalendar(
                    month = month,
                    highlightedDates = highlightedDates,
                    selectedDate = LocalDate.now(),
                    onDateClick = { }
                )
            }
        }
        item {
            ProgressRingCard(
                progressPercent = stats.monthlyCompletionRate,
                bestStreak = stats.bestStreak,
                perfectDays = perfectDays
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RoutineTrackCard(
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    cornerRadius = 26.dp
                ) {
                    Text("Weekly", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${stats.weeklyCompletionRate}%", style = MaterialTheme.typography.headlineSmall)
                    Text("completion rate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                RoutineTrackCard(
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    cornerRadius = 26.dp
                ) {
                    Text("Current", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${stats.currentStreak}", style = MaterialTheme.typography.headlineSmall)
                    Text("streak days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            RoutineTrackCard(cornerRadius = 30.dp) {
                Text("Report", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                SettingsRow(title = "Più costante", value = stats.mostCompletedHabit ?: "-")
                SettingsRow(title = "Da recuperare", value = stats.leastCompletedHabit ?: "-")
                SettingsRow(title = "Giorno migliore", value = stats.bestDayName ?: "-")
            }
        }
    }
}

private fun monthLabel(month: YearMonth): String {
    return "${month.monthValue.toString().padStart(2, '0')}/${month.year}"
}
