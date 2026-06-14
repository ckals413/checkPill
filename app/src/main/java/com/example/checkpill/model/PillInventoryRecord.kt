package com.example.checkpill.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pill_inventory_records")
data class PillInventoryRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pillName: String,
    val pillCount: Int,
    val savedAtMillis: Long,
    val savedAtText: String
)
