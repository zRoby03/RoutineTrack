package com.example.routinetrack.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.routinetrack.RoutineTrackApplication

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as RoutineTrackApplication
        val result = app.container.syncRepository.syncNow()
        return if (result.isSuccess) Result.success() else Result.retry()
    }
}
