package com.example.checkpill.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.checkpill.model.PillInventoryRecord

@Database(
    entities = [PillInventoryRecord::class],
    version = 2,
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
                    .addMigrations(MIGRATION_1_2)
                    .allowMainThreadQueries()
                    .build()
                    .also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pill_inventory_records ADD COLUMN transactionType TEXT NOT NULL DEFAULT 'IN'")
                db.execSQL("ALTER TABLE pill_inventory_records ADD COLUMN expirationDateText TEXT")
                db.execSQL("ALTER TABLE pill_inventory_records ADD COLUMN photoUri TEXT")
            }
        }
    }
}
