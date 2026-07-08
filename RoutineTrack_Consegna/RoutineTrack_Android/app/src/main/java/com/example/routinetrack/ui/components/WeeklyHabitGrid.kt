package com.example.routinetrack.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.routinetrack.domain.model.WeeklyHabitReport

@Composable
fun WeeklyHabitGrid(rows: List<WeeklyHabitReport>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Abitudine", modifier = Modifier.weight(1.4f), fontWeight = FontWeight.Bold)
            Text("Fatti", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
            Text("%", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold)
        }
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(row.habitTitle, modifier = Modifier.weight(1.4f), style = MaterialTheme.typography.bodyMedium)
                Text("${row.completedDays}/${row.expectedDays}", modifier = Modifier.weight(0.8f))
                Text("${row.rate}%", modifier = Modifier.weight(0.5f))
            }
        }
    }
}
