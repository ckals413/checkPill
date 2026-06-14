package com.example.checkpill.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.checkpill.model.PillInventoryRecord

@Database(
    entities = [PillInventoryRecord::class],
    version = 1,
    exportSchema = false
)
abstract class CheckPillDatabase : RoomDatabase() {
    abstract fun pillInventoryDao(): PillInventoryDao

    companion object {
        @Volatile
        private var instance: CheckPillDatabase? = null

        fun getInstance(context: Context): CheckPillDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CheckPillDatabase::class.java,
                    "check_pill.db"
                )
                    .allowMainThreadQueries()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
