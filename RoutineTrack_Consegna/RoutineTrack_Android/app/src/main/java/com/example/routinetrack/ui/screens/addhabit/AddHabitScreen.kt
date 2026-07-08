package com.example.routinetrack.ui.screens.addhabit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.routinetrack.domain.model.FrequencyMode
import com.example.routinetrack.domain.model.HabitCategory
import com.example.routinetrack.domain.model.HabitType
import com.example.routinetrack.domain.model.HabitUnit
import com.example.routinetrack.ui.components.CategoryChip
import com.example.routinetrack.ui.components.PrimaryKhakiButton
import com.example.routinetrack.ui.components.RoutineTrackCard
import com.example.routinetrack.ui.components.SettingsRow
import com.example.routinetrack.ui.components.routineEmoji
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val habitColors = listOf(
    "#C8B88A",
    "#9F8F62",
    "#8FA58A",
    "#B86B5F",
    "#D7B37A",
    "#EDE3C7",
    "#A8B59D",
    "#CC9F7A"
)
private val dayLabels = listOf(1 to "L", 2 to "M", 3 to "M", 4 to "G", 5 to "V", 6 to "S", 7 to "D")
private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    viewModel: AddHabitViewModel,
    onBackClick: () -> Unit,
    onSaved: (Long) -> Unit
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    var showNotificationDialog by rememberSaveable { mutableStateOf(false) }
    var notificationWarning by rememberSaveable { mutableStateOf<String?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                viewModel.updateReminderEnabled(true)
                notificationWarning = null
            } else {
                viewModel.updateReminderEnabled(false)
                notificationWarning = "Puoi riattivare le notifiche dalle impostazioni."
            }
        }
    )

    LaunchedEffect(state.savedHabitId) {
        state.savedHabitId?.let(onSaved)
    }

    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Notifiche reminder") },
            text = { Text("Accetta le notifiche per ricevere i promemoria delle tue routine.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    onClick = {
                        showNotificationDialog = false
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                ) {
                    Text("Accetta notifiche")
                }
            },
            dismissButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onClick = {
                        showNotificationDialog = false
                        viewModel.updateReminderEnabled(false)
                    }
                ) {
                    Text("Non ora")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 30.dp)
    ) {
        item {
            HabitTopBar(
                title = if (state.editingHabitId == null) "New Habit" else "Edit Habit",
                emoji = state.category.routineEmoji(),
                onBackClick = onBackClick
            )
        }

        item {
            RoutineTrackCard(cornerRadius = 30.dp) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                color = colorFromHex(state.color).copy(alpha = 0.45f),
                                shape = RoundedCornerShape(22.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.category.routineEmoji(),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = state.title,
                        onValueChange = viewModel::updateTitle,
                        label = { Text("Habit name") },
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.description,
                    onValueChange = viewModel::updateDescription,
                    label = { Text("Description (optional)") },
                    minLines = 2
                )
            }
        }

        item {
            RoutineTrackCard(cornerRadius = 30.dp) {
                Text("Category", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(HabitCategory.entries) { category ->
                        CategoryChip(
                            label = category.label,
                            icon = category.routineEmoji(),
                            selected = state.category == category,
                            onClick = { viewModel.updateCategory(category) }
                        )
                    }
                }
                Text("Color", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    habitColors.forEach { color ->
                        ColorCircle(
                            color = colorFromHex(color),
                            selected = state.color == color,
                            onClick = { viewModel.updateColor(color) }
                        )
                    }
                }
            }
        }

        item {
            RoutineTrackCard(cornerRadius = 30.dp) {
                Text("Daily goal", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryChip(
                        label = "1 check",
                        selected = state.type == HabitType.BOOLEAN,
                        onClick = { viewModel.updateType(HabitType.BOOLEAN) }
                    )
                    CategoryChip(
                        label = "Counter",
                        selected = state.type == HabitType.NUMERIC,
                        onClick = { viewModel.updateType(HabitType.NUMERIC) }
                    )
                }
                if (state.type == HabitType.NUMERIC) {
                    HabitUnitDropdown(
                        selected = state.unit,
                        onSelected = viewModel::updateUnit
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.targetValue,
                        onValueChange = viewModel::updateTargetValue,
                        label = {
                            Text(if (state.unit == HabitUnit.TIME) "Goal value hh:mm:ss" else "Goal value")
                        },
                        placeholder = {
                            Text(if (state.unit == HabitUnit.TIME) "00:30:00" else "5")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (state.unit == HabitUnit.TIME) {
                                KeyboardType.Text
                            } else {
                                KeyboardType.Number
                            }
                        ),
                        singleLine = true
                    )
                }
            }
        }

        item {
            RoutineTrackCard(cornerRadius = 30.dp) {
                Text("Active days", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryChip(
                        label = "Every day",
                        selected = state.frequencyMode == FrequencyMode.DAILY,
                        onClick = { viewModel.updateFrequencyMode(FrequencyMode.DAILY) }
                    )
                    CategoryChip(
                        label = "Custom",
                        selected = state.frequencyMode == FrequencyMode.SPECIFIC_DAYS,
                        onClick = { viewModel.updateFrequencyMode(FrequencyMode.SPECIFIC_DAYS) }
                    )
                }
                if (state.frequencyMode == FrequencyMode.SPECIFIC_DAYS) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dayLabels.forEach { (day, label) ->
                            DayChip(
                                label = label,
                                selected = state.selectedDays.contains(day),
                                onClick = { viewModel.toggleDay(day) }
                            )
                        }
                    }
                }
            }
        }

        item {
            RoutineTrackCard(cornerRadius = 30.dp) {
                Text("Habit term", style = MaterialTheme.typography.titleMedium)
                DateRow(
                    title = "Start date",
                    value = state.startDate.format(dateFormatter),
                    initialDate = state.startDate,
                    onDateSelected = viewModel::updateStartDate
                )
                DateRow(
                    title = "End date",
                    value = state.endDate?.format(dateFormatter) ?: "No end",
                    initialDate = state.endDate ?: state.startDate,
                    onDateSelected = { viewModel.updateEndDate(it) }
                )
                if (state.endDate != null) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.updateEndDate(null) }
                    ) {
                        Text("No end")
                    }
                }
            }
        }

        item {
            RoutineTrackCard(cornerRadius = 30.dp) {
                SettingsRow(
                    title = "Reminder",
                    trailing = {
                        RoutineSwitch(
                            checked = state.reminderEnabled,
                            onCheckedChange = { enabled ->
                                if (!enabled) {
                                    viewModel.updateReminderEnabled(false)
                                    notificationWarning = null
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    showNotificationDialog = true
                                } else {
                                    viewModel.updateReminderEnabled(true)
                                }
                            }
                        )
                    }
                )
                if (state.reminderEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimeNumberDropdown(
                            modifier = Modifier.weight(1f),
                            label = "Hour",
                            selected = state.reminderHour,
                            values = 0..23,
                            onSelected = viewModel::updateReminderHour
                        )
                        Text(":", style = MaterialTheme.typography.titleLarge)
                        TimeNumberDropdown(
                            modifier = Modifier.weight(1f),
                            label = "Minute",
                            selected = state.reminderMinute,
                            values = 0..59,
                            onSelected = viewModel::updateReminderMinute
                        )
                    }
                }
                notificationWarning?.let { warning ->
                    Text(
                        text = warning,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        state.errorMessage?.let { message ->
            item {
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
        }

        item {
            PrimaryKhakiButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving,
                text = if (state.isSaving) "Saving..." else "Save",
                onClick = viewModel::saveHabit,
                leadingIcon = {
                    Icon(Icons.Default.Check, contentDescription = null)
                }
            )
        }
    }
}

@Composable
private fun DateRow(
    title: String,
    value: String,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate? = null
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    SettingsRow(
        title = title,
        value = value,
        onClick = { showPicker = true }
    )
    if (showPicker) {
        RoutineDatePickerDialog(
            title = title,
            initialDate = initialDate ?: LocalDate.now(),
            onDismiss = { showPicker = false },
            onDateSelected = onDateSelected
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutineDatePickerDialog(
    title: String,
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochMillis()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis
                        ?.toLocalDateFromMillis()
                        ?.let(onDateSelected)
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = pickerState,
            title = {
                Text(
                    text = title,
                    modifier = Modifier.padding(start = 24.dp, top = 18.dp)
                )
            }
        )
    }
}

@Composable
private fun HabitTopBar(
    title: String,
    emoji: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun ColorCircle(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .padding(4.dp)
            .background(color, CircleShape)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun DayChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        shape = RoundedCornerShape(18.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitUnitDropdown(
    selected: HabitUnit,
    onSelected: (HabitUnit) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            value = selected.label,
            onValueChange = { },
            readOnly = true,
            label = { Text("Unit") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            HabitUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.label) },
                    onClick = {
                        expanded = false
                        onSelected(unit)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeNumberDropdown(
    label: String,
    selected: Int,
    values: IntRange,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            value = selected.twoDigits(),
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            values.forEach { value ->
                DropdownMenuItem(
                    text = { Text(value.twoDigits()) },
                    onClick = {
                        expanded = false
                        onSelected(value)
                    }
                )
            }
        }
    }
}

@Composable
private fun RoutineSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.surface,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.surface,
            uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

private fun colorFromHex(hex: String): Color {
    return runCatching { Color(android.graphics.Color.parseColor(hex)) }
        .getOrDefault(Color(0xFFC8B88A))
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')

private fun LocalDate.toEpochMillis(): Long {
    return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun Long.toLocalDateFromMillis(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}
