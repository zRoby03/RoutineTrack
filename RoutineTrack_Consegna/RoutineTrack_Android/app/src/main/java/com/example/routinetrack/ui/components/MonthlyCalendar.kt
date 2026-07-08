package com.example.routinetrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.routinetrack.ui.theme.KhakiPrimary
import com.example.routinetrack.ui.theme.KhakiSoft
import com.example.routinetrack.ui.theme.RoutineTextPrimary
import com.example.routinetrack.ui.theme.RoutineTextSecondary
import com.example.routinetrack.ui.theme.SageSoft
import com.example.routinetrack.ui.theme.SageSuccess
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthlyCalendar(
    month: YearMonth,
    highlightedDates: Set<LocalDate>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val weekdays = listOf("L", "M", "M", "G", "V", "S", "D")
    val firstDay = month.atDay(1)
    val leadingEmpty = firstDay.dayOfWeek.value - 1
    val days = buildList<LocalDate?> {
        repeat(leadingEmpty) { add(null) }
        for (day in 1..month.lengthOfMonth()) add(month.atDay(day))
        while (size % 7 != 0) add(null)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            weekdays.forEach { day ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        days.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                week.forEach { date ->
                    CalendarDayCell(
                        date = date,
                        highlighted = date != null && highlightedDates.contains(date),
                        selected = date != null && selectedDate == date,
                        onClick = { if (date != null) onDateClick(date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate?,
    highlighted: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = when {
        selected -> MaterialTheme.colorScheme.primary
        highlighted -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f)
    }
    val textColor = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        highlighted -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(if (date == null) Color.Transparent else background)
            .clickable(enabled = date != null, onClick = onClick)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
