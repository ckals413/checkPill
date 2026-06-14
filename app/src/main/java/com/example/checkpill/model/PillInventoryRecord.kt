package com.example.checkpill.model

data class PillInventoryRecord(
    val pillName: String,
    val pillCount: Int,
    val savedAtMillis: Long,
    val savedAtText: String
)
