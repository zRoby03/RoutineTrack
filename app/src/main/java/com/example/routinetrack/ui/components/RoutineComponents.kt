package com.example.routinetrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.routinetrack.domain.model.Habit
import com.example.routinetrack.domain.model.HabitCategory
import com.example.routinetrack.ui.theme.KhakiPrimary
import com.example.routinetrack.ui.theme.KhakiPrimaryDark
import com.example.routinetrack.ui.theme.KhakiSoft
import com.example.routinetrack.ui.theme.RoutineSurface
import com.example.routinetrack.ui.theme.RoutineTextPrimary
import com.example.routinetrack.ui.theme.RoutineTextSecondary
import com.example.routinetrack.ui.theme.SageSoft
import com.example.routinetrack.ui.theme.SageSuccess
import com.example.routinetrack.ui.theme.WarmDivider

@Composable
fun RoutineTrackCard(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    cornerRadius: Dp = 28.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val resolvedContainer = if (containerColor == Color.Unspecified) {
        MaterialTheme.colorScheme.surface
    } else {
        containerColor
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = resolvedContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: String? = null
) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (icon != null) {
                    Text(text = icon)
                }
                Text(text = label, fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(22.dp),
        border = null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
fun PrimaryKhakiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        modifier = modifier.height(54.dp),
        enabled = enabled,
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    RoutineTrackCard(modifier = modifier, cornerRadius = 30.dp) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        }
        content()
    }
}

@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val rowModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Row(
        modifier = rowModifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (leading != null) {
            leading()
        }
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProgressRingCard(
    progressPercent: Int,
    bestStreak: Int,
    perfectDays: Int,
    modifier: Modifier = Modifier,
    title: String = "Monthly Rate"
) {
    val progress = (progressPercent.coerceIn(0, 100) / 100f)

    RoutineTrackCard(modifier = modifier, cornerRadius = 32.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(214.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(176.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
                strokeWidth = 16.dp
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RingMetric(icon = "🏅", value = bestStreak, label = "Best Streaks")
            RingMetric(icon = "✓", value = perfectDays, label = "Perfect Days")
        }
    }
}

@Composable
private fun RingMetric(icon: String, value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = icon, style = MaterialTheme.typography.titleMedium)
            }
        }
        Text(
            text = if (value == 1) "1 Day" else "$value Days",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun habitEmojiFor(habit: Habit): String {
    val title = habit.title.lowercase()
    return when {
        "water" in title || "acqua" in title || "drink" in title -> "💧"
        "sleep" in title || "dormi" in title || "sonno" in title -> "🛏️"
        "walk" in title || "cammin" in title -> "🚶"
        "run" in title || "corsa" in title -> "🏃"
        "workout" in title || "palestra" in title || "fitness" in title -> "💪"
        "study" in title || "studio" in title || "read" in title || "leggi" in title -> "📚"
        "meditation" in title || "medita" in title || "mind" in title -> "🧘"
        "coffee" in title || "caffe" in title -> "☕"
        else -> habit.category.routineEmoji()
    }
}

fun HabitCategory.routineEmoji(): String {
    return when (this) {
        HabitCategory.HEALTH -> "❤"
        HabitCategory.FITNESS -> "💪"
        HabitCategory.STUDY -> "📚"
        HabitCategory.MINDFULNESS -> "🧘"
        HabitCategory.WATER -> "💧"
        HabitCategory.OTHER -> "★"
    }
}

fun HabitCategory.routineAccentColor(): Color {
    return when (this) {
        HabitCategory.HEALTH -> Color(0xFFF1DCD6)
        HabitCategory.FITNESS -> Color(0xFFE8ECD9)
        HabitCategory.STUDY -> Color(0xFFEAE3D0)
        HabitCategory.MINDFULNESS -> Color(0xFFE9E4D8)
        HabitCategory.WATER -> Color(0xFFE4EDF0)
        HabitCategory.OTHER -> KhakiSoft
    }
}

@Composable
fun SmallStatusPill(text: String, color: Color = Color.Unspecified) {
    val resolvedColor = if (color == Color.Unspecified) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        color
    }
    Box(
        modifier = Modifier
            .background(color = resolvedColor, shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (resolvedColor == MaterialTheme.colorScheme.secondary) {
                MaterialTheme.colorScheme.onSecondary
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            },
            fontWeight = FontWeight.Bold
        )
    }
}
