package com.example.routinetrack.sync

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.routinetrack.data.repository.SyncRepository

class SyncManager(
    private val context: Context,
    private val syncRepository: SyncRepository
) {
    fun enqueueSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "routine_track_sync",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    suspend fun syncNow(): Result<Unit> {
        return syncRepository.syncNow()
    }

    suspend fun restoreFromCloud(): Result<Unit> {
        return syncRepository.restoreFromCloud()
    }
}
