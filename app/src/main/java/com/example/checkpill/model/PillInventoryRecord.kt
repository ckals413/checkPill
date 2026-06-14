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
    val savedAtText: String,
    val transactionType: String = TYPE_IN,
    val expirationDateText: String? = null,
    val photoUri: String? = null
) {
    fun signedCount(): Int = if (transactionType == TYPE_OUT) -pillCount else pillCount

    fun transactionLabel(): String = if (transactionType == TYPE_OUT) "출고" else "입고"

    companion object {
        const val TYPE_IN = "IN"
        const val TYPE_OUT = "OUT"
    }
}
