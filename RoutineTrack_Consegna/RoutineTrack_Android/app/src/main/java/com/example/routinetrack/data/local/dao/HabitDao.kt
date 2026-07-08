package com.example.routinetrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.routinetrack.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    // DAO = Data Access Object: qui vivono le query sulla tabella habits.
    // Flow permette alla UI di ricevere automaticamente gli aggiornamenti del database.
    @Query("SELECT * FROM habits WHERE user_id = :userId AND is_deleted = 0 ORDER BY created_at DESC")
    fun observeHabitsForUser(userId: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE user_id = :userId AND is_deleted = 0 ORDER BY created_at DESC")
    suspend fun getHabitsForUserOnce(userId: String): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    fun getHabitById(id: Long): Flow<HabitEntity?>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitByIdOnce(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("UPDATE habits SET is_deleted = 1, pending_sync = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeleteHabit(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun hardDeleteHabit(id: Long)

    @Query("SELECT * FROM habits WHERE user_id = :userId AND pending_sync = 1")
    suspend fun getPendingHabits(userId: String): List<HabitEntity>

    @Query("SELECT COUNT(*) FROM habits WHERE user_id = :userId AND is_deleted = 0")
    fun countActiveHabitsForUser(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM habits WHERE user_id = :userId AND pending_sync = 1")
    fun countPendingHabitsForUser(userId: String): Flow<Int>

    @Query("SELECT * FROM habits WHERE remote_id = :remoteId LIMIT 1")
    suspend fun getHabitByRemoteId(remoteId: String): HabitEntity?

    @Query("UPDATE habits SET remote_id = :remoteId, pending_sync = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateSyncState(
        id: Long,
        remoteId: String?,
        updatedAt: Long = System.currentTimeMillis()
    )
}
