package com.example.checkpill.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.checkpill.model.PillInventoryRecord

@Dao
interface PillInventoryDao {
    @Query("SELECT * FROM pill_inventory_records ORDER BY savedAtMillis DESC")
    fun getRecords(): List<PillInventoryRecord>

    @Query("SELECT * FROM pill_inventory_records WHERE id = :id")
    fun getRecord(id: Long): PillInventoryRecord?

    @Insert
    fun insert(record: PillInventoryRecord): Long

    @Update
    fun update(record: PillInventoryRecord)

    @Delete
    fun delete(record: PillInventoryRecord)
}
