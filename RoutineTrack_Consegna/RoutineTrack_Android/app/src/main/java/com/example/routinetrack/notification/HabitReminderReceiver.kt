package com.example.routinetrack.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class HabitReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        val title = intent.getStringExtra(EXTRA_HABIT_TITLE).orEmpty().ifBlank { "abitudine" }
        val hour = intent.getIntExtra(EXTRA_HOUR, -1)
        val minute = intent.getIntExtra(EXTRA_MINUTE, -1)

        NotificationHelper.showHabitReminder(context, habitId, title)

        if (habitId > 0 && hour in 0..23 && minute in 0..59) {
            ReminderScheduler(context).schedule(habitId, title, hour, minute)
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "habit_id"
        const val EXTRA_HABIT_TITLE = "habit_title"
        const val EXTRA_HOUR = "reminder_hour"
        const val EXTRA_MINUTE = "reminder_minute"
    }
}
