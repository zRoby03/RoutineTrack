package com.example.routinetrack.ui.screens.settings

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.routinetrack.R
import com.example.routinetrack.domain.model.AppThemeMode
import com.example.routinetrack.ui.components.PrimaryKhakiButton
import com.example.routinetrack.ui.components.SettingsGroup
import com.example.routinetrack.ui.components.SettingsRow
import com.example.routinetrack.ui.components.SmallStatusPill
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    currentThemeMode: AppThemeMode,
    onThemeModeChanged: (AppThemeMode) -> Unit,
    onLauncherIconApply: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    var showBirthdayPicker by rememberSaveable { mutableStateOf(false) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.saveProfilePhoto(uri)
        }
    }

    if (showBirthdayPicker) {
        BirthdayPickerDialog(
            initialDate = state.birthdayIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: LocalDate.now().minusYears(18),
            onDismiss = { showBirthdayPicker = false },
            onDateSelected = { date ->
                viewModel.saveBirthday(date)
                showBirthdayPicker = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 26.dp, bottom = 28.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileAvatar(
                    photoPath = state.profilePhotoPath,
                    fallbackInitial = state.user?.displayName?.take(1)?.uppercase()
                        ?: state.user?.email?.take(1)?.uppercase()
                        ?: "R",
                    onClick = { photoPickerLauncher.launch("image/*") }
                )
                androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.user?.displayName ?: "RoutineTrack",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = state.user?.email ?: "Sign in to sync your routines",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SmallStatusPill(
                        text = "${state.counts.activeHabits} habits",
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }
        }
        item {
            SettingsGroup(title = "Account") {
                SettingsRow(
                    title = "Compleanno",
                    value = state.birthdayLabel,
                    onClick = { showBirthdayPicker = true }
                )
            }
        }
        item {
            SettingsGroup(title = "Theme") {
                Text(
                    text = "Scegli l'aspetto globale. L'icona launcher si aggiorna solo con il bottone dedicato.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    ThemeIconOption(
                        modifier = Modifier.weight(1f),
                        label = AppThemeMode.LIGHT.label,
                        imageRes = R.drawable.icon_light,
                        selected = currentThemeMode == AppThemeMode.LIGHT,
                        onClick = { onThemeModeChanged(AppThemeMode.LIGHT) }
                    )
                    ThemeIconOption(
                        modifier = Modifier.weight(1f),
                        label = AppThemeMode.DARK.label,
                        imageRes = R.drawable.icon_dark,
                        selected = currentThemeMode == AppThemeMode.DARK,
                        onClick = { onThemeModeChanged(AppThemeMode.DARK) }
                    )
                }
                PrimaryKhakiButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Aggiorna icona launcher",
                    onClick = onLauncherIconApply
                )
            }
        }
        item {
            SettingsGroup(title = "Data & Sync") {
                SettingsRow(title = "Backend URL", value = state.baseUrl)
                SettingsRow(title = "Last sync", value = state.lastSyncLabel)
                SettingsRow(title = "Unsynced items", value = state.counts.unsyncedItems.toString())
                state.message?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PrimaryKhakiButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSyncing,
                    text = if (state.isSyncing) "Sync..." else "Sync Now",
                    onClick = viewModel::syncNow,
                    leadingIcon = {
                        Icon(Icons.Default.Sync, contentDescription = null)
                    }
                )
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSyncing,
                    onClick = viewModel::restoreFromCloud
                ) {
                    Text("Restore from Cloud")
                }
            }
        }
        item {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.logout(onLoggedOut) }
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Text("Logout")
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    photoPath: String?,
    fallbackInitial: String,
    onClick: () -> Unit
) {
    val bitmap = remember(photoPath) {
        photoPath?.let { path -> BitmapFactory.decodeFile(path)?.asImageBitmap() }
    }

    Box(
        modifier = Modifier
            .size(82.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Foto profilo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = fallbackInitial,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdayPickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val pickerState = androidx.compose.material3.rememberDatePickerState(
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
    ) {
        DatePicker(state = pickerState)
    }
}

@Composable
private fun ThemeIconOption(
    label: String,
    imageRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    }

    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = label,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(22.dp)),
            contentScale = ContentScale.Crop
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        if (selected) {
            SmallStatusPill(text = "Selected")
        }
    }
}

private fun LocalDate.toEpochMillis(): Long {
    return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun Long.toLocalDateFromMillis(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}
