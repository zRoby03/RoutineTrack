package com.example.routinetrack.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_HABIT_TITLE) ?: "abitudine"
        NotificationHelper.showHabitReminder(applicationContext, title.hashCode().toLong(), title)
        return Result.success()
    }

    companion object {
        const val KEY_HABIT_TITLE = "habit_title"
    }
}
