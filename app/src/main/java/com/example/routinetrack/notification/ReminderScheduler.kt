package com.example.routinetrack.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(habitId: Long, title: String, hour: Int, minute: Int) {
        NotificationHelper.createNotificationChannel(context)
        alarmManager.cancel(pendingIntent(habitId, title, hour, minute))
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextTriggerMillis(hour, minute),
            pendingIntent(habitId, title, hour, minute)
        )
    }

    fun cancel(habitId: Long) {
        alarmManager.cancel(pendingIntent(habitId, "", 0, 0))
    }

    private fun pendingIntent(habitId: Long, title: String, hour: Int, minute: Int): PendingIntent {
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_TITLE, title)
            putExtra(HabitReminderReceiver.EXTRA_HOUR, hour)
            putExtra(HabitReminderReceiver.EXTRA_MINUTE, minute)
        }
        return PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        var trigger = now.toLocalDate().atTime(hour, minute)
        if (!trigger.isAfter(now)) {
            trigger = trigger.plusDays(1)
        }
        return trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
