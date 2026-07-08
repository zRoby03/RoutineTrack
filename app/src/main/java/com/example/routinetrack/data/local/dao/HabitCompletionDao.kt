package com.example.routinetrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.routinetrack.data.local.entity.HabitCompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {
    @Query("SELECT * FROM habit_completions WHERE user_id = :userId AND is_deleted = 0 ORDER BY date DESC")
    fun observeCompletionsForUser(userId: String): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE user_id = :userId AND is_deleted = 0 ORDER BY date DESC")
    suspend fun getCompletionsForUserOnce(userId: String): List<HabitCompletionEntity>

    @Query("SELECT * FROM habit_completions WHERE habit_id = :habitId AND is_deleted = 0 ORDER BY date DESC")
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE user_id = :userId AND date = :date AND is_deleted = 0 ORDER BY created_at DESC")
    fun getCompletionsByDate(userId: String, date: String): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate AND is_deleted = 0 ORDER BY date DESC")
    fun getCompletionsInRange(userId: String, startDate: String, endDate: String): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habit_id = :habitId AND user_id = :userId AND date = :date LIMIT 1")
    suspend fun getCompletionForHabitOnDate(habitId: Long, userId: String, date: String): HabitCompletionEntity?

    @Query("SELECT * FROM habit_completions WHERE id = :id LIMIT 1")
    suspend fun getCompletionByIdOnce(id: Long): HabitCompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCompletion(completion: HabitCompletionEntity): Long

    @Query("DELETE FROM habit_completions WHERE habit_id = :habitId AND date = :date")
    suspend fun deleteCompletion(habitId: Long, date: String)

    @Query("SELECT * FROM habit_completions WHERE user_id = :userId AND pending_sync = 1")
    suspend fun getPendingCompletions(userId: String): List<HabitCompletionEntity>

    @Query("SELECT COUNT(*) FROM habit_completions WHERE user_id = :userId AND pending_sync = 1")
    fun countPendingCompletionsForUser(userId: String): Flow<Int>

    @Query("SELECT * FROM habit_completions WHERE remote_id = :remoteId LIMIT 1")
    suspend fun getCompletionByRemoteId(remoteId: String): HabitCompletionEntity?

    @Query("UPDATE habit_completions SET remote_id = :remoteId, pending_sync = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateSyncState(id: Long, remoteId: String?, updatedAt: Long = System.currentTimeMillis())
}
