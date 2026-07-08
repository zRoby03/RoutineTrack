package com.example.routinetrack.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.routinetrack.domain.model.HabitType
import com.example.routinetrack.ui.components.EmptyState
import com.example.routinetrack.ui.components.ProgressRingCard
import com.example.routinetrack.ui.components.RoutineTrackCard
import com.example.routinetrack.ui.components.SettingsRow
import com.example.routinetrack.ui.components.habitEmojiFor
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HabitDetailScreen(
    viewModel: HabitDetailViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val habit = state.habit

    if (habit == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {
            EmptyState(
                title = "Abitudine non trovata",
                message = "Potrebbe essere stata eliminata."
            )
        }
        return
    }

    val completionDates = state.completions
        .filter { it.completed }
        .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
        .toSet()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 30.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                }
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = habitEmojiFor(habit), style = MaterialTheme.typography.headlineSmall)
                    Text(
                        modifier = Modifier.padding(start = 10.dp),
                        text = habit.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(modifier = Modifier.size(48.dp))
            }
        }
        item {
            RoutineTrackCard(cornerRadius = 30.dp) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(colorFromHex(habit.color).copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = habitEmojiFor(habit), style = MaterialTheme.typography.headlineMedium)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(habit.title, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = habit.description.ifBlank { habit.category.label },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                SettingsRow(title = "Categoria", value = habit.category.label)
                SettingsRow(title = "Frequenza", value = habit.frequency.toStorage())
                SettingsRow(
                    title = "Target",
                    value = targetLabel(habit.type, habit.targetValue, habit.unit)
                )
                SettingsRow(title = "Start date", value = formatStoredDate(habit.startDate))
                SettingsRow(title = "End date", value = habit.endDate?.let(::formatStoredDate) ?: "No end")
                SettingsRow(title = "Reminder", value = habit.reminderTime ?: "Off")
                SettingsRow(
                    title = "Colore",
                    trailing = {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(colorFromHex(habit.color))
                        )
                    }
                )
            }
        }
        item {
            RoutineTrackCard(cornerRadius = 30.dp) {
                Text("Oggi", style = MaterialTheme.typography.titleMedium)
                SettingsRow(
                    title = "Progress",
                    value = todayProgressLabel(
                        type = habit.type,
                        value = state.todayCompletion?.value ?: 0.0,
                        completed = state.todayCompletion?.completed == true,
                        target = habit.targetValue ?: 1.0,
                        unit = habit.unit
                    )
                )
                SettingsRow(
                    title = "Status",
                    value = if (state.todayCompletion?.completed == true) "Completata" else "Da completare"
                )
            }
        }
        item {
            ProgressRingCard(
                progressPercent = state.stats.monthlyCompletionRate,
                bestStreak = state.stats.bestStreak,
                perfectDays = completionDates.count { YearMonth.from(it) == YearMonth.now() }
            )
        }
        item {
            RoutineTrackCard(cornerRadius = 30.dp) {
                Text("Report", style = MaterialTheme.typography.titleMedium)
                SettingsRow(title = "Totale completamenti", value = state.stats.totalCompletions.toString())
                SettingsRow(title = "Current streak", value = "${state.stats.currentStreak} giorni")
                SettingsRow(title = "Best streak", value = "${state.stats.bestStreak} giorni")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { onEdit(habit.id) }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Text("Modifica")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.deleteHabit(onBack) }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("Elimina")
                }
            }
        }
    }
}

private fun targetLabel(type: HabitType, targetValue: Double?, unit: String?): String {
    if (type != HabitType.NUMERIC) return "1 completamento"
    val target = targetValue ?: 0.0
    return if (unit == "Tempo") {
        secondsToDurationText(target.toInt())
    } else {
        "${target.cleanNumber()} ${unit.orEmpty()}".trim()
    }
}

private fun todayProgressLabel(
    type: HabitType,
    value: Double,
    completed: Boolean,
    target: Double,
    unit: String?
): String {
    if (type != HabitType.NUMERIC) return if (completed) "1 / 1" else "0 / 1"
    return if (unit == "Tempo") {
        "${secondsToDurationText(value.toInt())} / ${secondsToDurationText(target.toInt())}"
    } else {
        "${value.cleanNumber()} / ${target.cleanNumber()} ${unit.orEmpty()}".trim()
    }
}

private fun Double.cleanNumber(): String {
    return if (this % 1.0 == 0.0) toInt().toString() else toString()
}

private fun secondsToDurationText(totalSeconds: Int): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0)
    val hours = safeSeconds / 3600
    val minutes = (safeSeconds % 3600) / 60
    val seconds = safeSeconds % 60
    return "${hours.twoDigits()}:${minutes.twoDigits()}:${seconds.twoDigits()}"
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')

private fun formatStoredDate(value: String): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN)
    return runCatching { LocalDate.parse(value).format(formatter) }.getOrDefault(value)
}

private fun colorFromHex(hex: String): Color {
    return runCatching { Color(android.graphics.Color.parseColor(hex)) }
        .getOrDefault(Color(0xFFC8B88A))
}
