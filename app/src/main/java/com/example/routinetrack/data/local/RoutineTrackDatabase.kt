package com.example.routinetrack.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.routinetrack.data.local.dao.HabitCompletionDao
import com.example.routinetrack.data.local.dao.HabitDao
import com.example.routinetrack.data.local.dao.UserDao
import com.example.routinetrack.data.local.entity.HabitCompletionEntity
import com.example.routinetrack.data.local.entity.HabitEntity
import com.example.routinetrack.data.local.entity.UserEntity

@Database(
    entities = [
        HabitEntity::class,
        HabitCompletionEntity::class,
        UserEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class RoutineTrackDatabase : RoomDatabase() {
    // Ogni DAO espone le query per una tabella specifica: la UI non accede mai qui direttamente.
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: RoutineTrackDatabase? = null

        fun getDatabase(context: Context): RoutineTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RoutineTrackDatabase::class.java,
                    "routine_track.db"
                )
                    // Singleton thread-safe: evita di aprire più connessioni Room alla stessa base dati.
                    // In un progetto universitario iniziale consente di iterare sullo schema.
                    // In produzione servirebbero migration esplicite.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
