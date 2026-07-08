package com.example.routinetrack.ui.screens.settings

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    currentThemeMode: AppThemeMode,
    onThemeModeChanged: (AppThemeMode) -> Unit,
    onLauncherIconApply: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

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
                    fallbackInitial = state.user?.displayName?.take(1)?.uppercase()
                        ?: state.user?.email?.take(1)?.uppercase()
                        ?: "R"
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
                SettingsRow(title = "Cloud URL", value = state.baseUrl)
                SettingsRow(title = "Ultima sync", value = state.lastSyncLabel)
                SettingsRow(title = "Elementi da sincronizzare", value = state.counts.unsyncedItems.toString())
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
                    text = if (state.isSyncing) "Sincronizzo..." else "Sincronizza ora",
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
                    Text("Ripristina dal cloud")
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
    fallbackInitial: String
) {
    Box(
        modifier = Modifier
            .size(82.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = fallbackInitial,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
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
