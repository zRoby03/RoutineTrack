package com.example.routinetrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.routinetrack.domain.model.Habit
import com.example.routinetrack.domain.model.HabitType
import com.example.routinetrack.ui.theme.KhakiPrimary
import com.example.routinetrack.ui.theme.KhakiSoft
import com.example.routinetrack.ui.theme.RoutineSurface
import com.example.routinetrack.ui.theme.RoutineTextPrimary
import com.example.routinetrack.ui.theme.RoutineTextSecondary
import com.example.routinetrack.ui.theme.SageSoft
import com.example.routinetrack.ui.theme.SageSuccess
import kotlinx.coroutines.withTimeoutOrNull

private const val MANUAL_PROGRESS_HOLD_MS = 2_000L

@Composable
fun RoutineHabitCard(
    habit: Habit,
    progressValue: Double,
    completed: Boolean,
    currentStreak: Int,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onIncrementProgress: () -> Unit,
    onDecrementProgress: () -> Unit,
    onManualProgressRequest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val target = habit.targetValue ?: 1.0
    val progress = when (habit.type) {
        HabitType.BOOLEAN -> if (completed) 1f else 0f
        HabitType.NUMERIC -> (progressValue / target).toFloat().coerceIn(0f, 1f)
    }
    val container = if (completed) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val actionColor = if (completed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary

    RoutineTrackCard(
        modifier = modifier.clickable(onClick = onClick),
        containerColor = container,
        cornerRadius = 30.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                color = habit.category.routineAccentColor()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = habitEmojiFor(habit),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallStatusPill(
                        text = progressLabel(habit, progressValue, completed),
                        color = if (completed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer
                    )
                    if (habit.description.isNotBlank()) {
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🔥 ${streakLabel(currentStreak)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (habit.type == HabitType.NUMERIC && progressValue > 0.0) {
                        RoundActionButton(
                            onClick = onDecrementProgress,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            background = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Riduci")
                        }
                    }
                    RoundActionButton(
                        onClick = if (habit.type == HabitType.NUMERIC) {
                            onIncrementProgress
                        } else {
                            onToggleComplete
                        },
                        onLongPress = if (habit.type == HabitType.NUMERIC) {
                            onManualProgressRequest
                        } else {
                            null
                        },
                        tint = if (completed) MaterialTheme.colorScheme.onSecondary else actionColor,
                        background = if (completed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                    ) {
                        Icon(
                            imageVector = if (habit.type == HabitType.NUMERIC) {
                                Icons.Default.Add
                            } else {
                                Icons.Default.Check
                            },
                            contentDescription = if (completed) "Completata" else "Aggiorna"
                        )
                    }
                }
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(CircleShape),
            color = if (completed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun RoundActionButton(
    onClick: () -> Unit,
    onLongPress: (() -> Unit)? = null,
    tint: Color,
    background: Color,
    content: @Composable () -> Unit
) {
    val interactionModifier = if (onLongPress == null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier.tapOrHold(onTap = onClick, onLongPress = onLongPress)
    }

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(background)
            .then(interactionModifier),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides tint,
            content = content
        )
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    progressValue: Double,
    completed: Boolean,
    currentStreak: Int,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onIncrementProgress: () -> Unit,
    onDecrementProgress: () -> Unit
) {
    RoutineHabitCard(
        habit = habit,
        progressValue = progressValue,
        completed = completed,
        currentStreak = currentStreak,
        onClick = onClick,
        onToggleComplete = onToggleComplete,
        onIncrementProgress = onIncrementProgress,
        onDecrementProgress = onDecrementProgress
    )
}

@Composable
fun CompactHabitCard(
    title: String,
    subtitle: String,
    color: String,
    onClick: () -> Unit
) {
    RoutineTrackCard(
        modifier = Modifier.clickable(onClick = onClick),
        containerColor = MaterialTheme.colorScheme.surface,
        cornerRadius = 24.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = title.take(1).uppercase(), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun progressLabel(habit: Habit, progressValue: Double, completed: Boolean): String {
    return when (habit.type) {
        HabitType.BOOLEAN -> if (completed) "1/1" else "0/1"
        HabitType.NUMERIC -> {
            val target = habit.targetValue ?: 0.0
            if (habit.unit == "Tempo") {
                return "${secondsToDurationText(progressValue.toInt())}/${secondsToDurationText(target.toInt())}"
            }
            val unit = habit.unit.orEmpty().takeIf { it.isNotBlank() && it != "volte" }
                ?.let { " $it" }
                .orEmpty()
            "${progressValue.cleanNumber()}/${target.cleanNumber()}$unit"
        }
    }
}

private fun streakLabel(value: Int): String {
    return if (value == 1) "1 Day" else "$value Days"
}

private fun Double.cleanNumber(): String {
    return if (this % 1.0 == 0.0) toInt().toString() else String.format("%.1f", this)
}

private fun secondsToDurationText(totalSeconds: Int): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0)
    val hours = safeSeconds / 3600
    val minutes = (safeSeconds % 3600) / 60
    val seconds = safeSeconds % 60
    return "${hours.twoDigits()}:${minutes.twoDigits()}:${seconds.twoDigits()}"
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')

private fun Modifier.tapOrHold(
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    holdMillis: Long = MANUAL_PROGRESS_HOLD_MS
): Modifier {
    return pointerInput(onTap, onLongPress, holdMillis) {
        detectTapGestures(
            onPress = {
                val releasedBeforeHold = withTimeoutOrNull(holdMillis) {
                    tryAwaitRelease()
                }
                when (releasedBeforeHold) {
                    true -> onTap()
                    false -> Unit
                    null -> {
                        onLongPress()
                        tryAwaitRelease()
                    }
                }
            }
        )
    }
}
