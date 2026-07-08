package com.example.routinetrack.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.routinetrack.domain.model.HabitType
import com.example.routinetrack.ui.components.EmptyState
import com.example.routinetrack.ui.components.RoutineHabitCard
import com.example.routinetrack.ui.components.WeekCalendarBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onHabitClick: (Long) -> Unit
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.ITALIAN)
    var manualProgressItem by remember { mutableStateOf<HabitTodayItem?>(null) }

    manualProgressItem?.let { item ->
        ManualProgressDialog(
            item = item,
            onDismiss = { manualProgressItem = null },
            onConfirm = { value ->
                viewModel.setManualProgress(item, value)
                manualProgressItem = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 28.dp)
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            WeekNavigator(
                selectedDate = state.selectedDate,
                formatter = formatter,
                onPreviousWeek = viewModel::previousWeek,
                onNextWeek = viewModel::nextWeek
            )
        }
        item {
            WeekCalendarBar(
                selectedDate = state.selectedDate,
                onDateSelected = viewModel::selectDate
            )
        }
        state.errorMessage?.let { message ->
            item {
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
        }
        if (state.items.isEmpty()) {
            item {
                EmptyState(
                    title = "Nessuna abitudine per oggi",
                    message = "Premi + per creare la prima routine della giornata."
                )
            }
        } else {
            items(state.items, key = { it.habit.id }) { item ->
                RoutineHabitCard(
                    habit = item.habit,
                    progressValue = item.progressValue,
                    completed = item.completed,
                    currentStreak = item.currentStreak,
                    onClick = { onHabitClick(item.habit.id) },
                    onToggleComplete = { viewModel.toggleComplete(item) },
                    onIncrementProgress = { viewModel.incrementProgress(item) },
                    onDecrementProgress = { viewModel.decrementProgress(item) },
                    onManualProgressRequest = { manualProgressItem = item }
                )
            }
        }
    }
}

@Composable
private fun ManualProgressDialog(
    item: HabitTodayItem,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val unit = item.habit.unit.orEmpty()
    val isTime = unit == "Tempo"
    val target = item.habit.targetValue ?: 1.0

    if (item.habit.type != HabitType.NUMERIC) return

    if (isTime) {
        TimeProgressDialogContent(
            currentSeconds = item.progressValue.toInt().coerceAtLeast(0),
            targetSeconds = target.toInt().coerceAtLeast(0),
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    } else {
        NumberProgressDialogContent(
            unit = unit,
            currentValue = item.progressValue.toInt().coerceAtLeast(0),
            targetValue = target.toInt().coerceAtLeast(0),
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
private fun NumberProgressDialogContent(
    unit: String,
    currentValue: Int,
    targetValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var input by rememberSaveable(currentValue) { mutableStateOf(currentValue.takeIf { it > 0 }?.toString().orEmpty()) }
    val parsedValue = input.toIntOrNull()
    val cleanedUnit = unit.takeIf { it.isNotBlank() } ?: "valore"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Inserisci progresso") },
        text = {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Target: $targetValue $cleanedUnit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { value ->
                        input = value.filter { it.isDigit() }.take(9)
                    },
                    label = { Text("Progresso") },
                    suffix = { Text(cleanedUnit) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = input.isNotBlank() && parsedValue == null
                )
            }
        },
        confirmButton = {
            Button(
                enabled = parsedValue != null && parsedValue >= 0,
                onClick = { onConfirm((parsedValue ?: 0).toDouble()) }
            ) {
                Text("Salva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
private fun TimeProgressDialogContent(
    currentSeconds: Int,
    targetSeconds: Int,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var hours by rememberSaveable(currentSeconds) { mutableStateOf((currentSeconds / 3600).toString()) }
    var minutes by rememberSaveable(currentSeconds) { mutableStateOf(((currentSeconds % 3600) / 60).toString()) }
    var seconds by rememberSaveable(currentSeconds) { mutableStateOf((currentSeconds % 60).toString()) }
    val parsedHours = hours.toIntOrNull()
    val parsedMinutes = minutes.toIntOrNull()
    val parsedSeconds = seconds.toIntOrNull()
    val valid = parsedHours != null &&
        parsedHours >= 0 &&
        parsedMinutes != null &&
        parsedMinutes in 0..59 &&
        parsedSeconds != null &&
        parsedSeconds in 0..59

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Inserisci progresso") },
        text = {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Target: ${formatDuration(targetSeconds)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimePartField(
                        modifier = Modifier.weight(1f),
                        label = "Ore",
                        value = hours,
                        onValueChange = { hours = it.filter(Char::isDigit).take(3) }
                    )
                    TimePartField(
                        modifier = Modifier.weight(1f),
                        label = "Min",
                        value = minutes,
                        onValueChange = { minutes = it.filter(Char::isDigit).take(2) },
                        isError = parsedMinutes != null && parsedMinutes !in 0..59
                    )
                    TimePartField(
                        modifier = Modifier.weight(1f),
                        label = "Sec",
                        value = seconds,
                        onValueChange = { seconds = it.filter(Char::isDigit).take(2) },
                        isError = parsedSeconds != null && parsedSeconds !in 0..59
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = valid,
                onClick = {
                    val total = ((parsedHours ?: 0) * 3600) +
                        ((parsedMinutes ?: 0) * 60) +
                        (parsedSeconds ?: 0)
                    onConfirm(total.toDouble())
                }
            ) {
                Text("Salva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
private fun TimePartField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = isError
    )
}

@Composable
private fun WeekNavigator(
    selectedDate: LocalDate,
    formatter: DateTimeFormatter,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousWeek) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Settimana precedente")
        }
        Text(
            text = selectedDate.format(formatter).replaceFirstChar { it.titlecase(Locale.ITALIAN) },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNextWeek) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Settimana successiva")
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0)
    val hours = safeSeconds / 3600
    val minutes = (safeSeconds % 3600) / 60
    val seconds = safeSeconds % 60
    return "${hours.twoDigits()}:${minutes.twoDigits()}:${seconds.twoDigits()}"
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')
