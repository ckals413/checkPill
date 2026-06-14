package com.example.checkpill.data

import android.content.Context
import com.example.checkpill.model.PillInventoryRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PillInventoryStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getRecords(): List<PillInventoryRecord> {
        val json = prefs.getString(KEY_RECORDS, null) ?: return emptyList()
        val type = object : TypeToken<List<PillInventoryRecord>>() {}.type
        return runCatching {
            gson.fromJson<List<PillInventoryRecord>>(json, type)
        }.getOrDefault(emptyList())
    }

    fun addRecord(record: PillInventoryRecord) {
        val updatedRecords = listOf(record) + getRecords()
        prefs.edit()
            .putString(KEY_RECORDS, gson.toJson(updatedRecords))
            .apply()
    }

    fun getTotalCount(): Int = getRecords().sumOf { it.pillCount }

    fun getTotalsByPillName(): List<Pair<String, Int>> {
        return getRecords()
            .groupBy { it.pillName }
            .map { (name, records) -> name to records.sumOf { it.pillCount } }
            .sortedBy { it.first }
    }

    companion object {
        private const val PREF_NAME = "pill_inventory"
        private const val KEY_RECORDS = "records"
    }
}
