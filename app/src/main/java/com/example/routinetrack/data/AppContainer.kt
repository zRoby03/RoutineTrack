package com.example.routinetrack.data

import android.content.Context
import com.example.routinetrack.data.local.RoutineTrackDatabase
import com.example.routinetrack.data.preferences.ProfilePreferenceRepository
import com.example.routinetrack.data.preferences.ThemePreferenceRepository
import com.example.routinetrack.data.remote.RetrofitClient
import com.example.routinetrack.data.repository.AuthRepository
import com.example.routinetrack.data.repository.HabitRepository
import com.example.routinetrack.data.repository.SyncRepository
import com.example.routinetrack.data.repository.StatsRepository
import com.example.routinetrack.data.session.SessionManager
import com.example.routinetrack.notification.ReminderScheduler
import com.example.routinetrack.sync.SyncManager

class AppContainer(context: Context) {
    private val database = RoutineTrackDatabase.getDatabase(context)
    private val apiService = RetrofitClient.create()
    private val reminderScheduler = ReminderScheduler(context)
    private val sessionManager = SessionManager(context)

    val themePreferenceRepository = ThemePreferenceRepository(context)
    val profilePreferenceRepository = ProfilePreferenceRepository(context)

    val authRepository = AuthRepository(
        apiService = apiService,
        userDao = database.userDao(),
        sessionManager = sessionManager
    )

    val habitRepository = HabitRepository(
        habitDao = database.habitDao(),
        completionDao = database.habitCompletionDao(),
        userDao = database.userDao(),
        reminderScheduler = reminderScheduler
    )

    val statsRepository = StatsRepository(
        habitDao = database.habitDao(),
        completionDao = database.habitCompletionDao(),
        userDao = database.userDao()
    )

    val syncRepository = SyncRepository(
        apiService = apiService,
        habitDao = database.habitDao(),
        completionDao = database.habitCompletionDao(),
        userDao = database.userDao(),
        sessionManager = sessionManager
    )

    val syncManager = SyncManager(
        context = context,
        syncRepository = syncRepository
    )
}
