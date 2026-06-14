package com.example.checkpill.data

import android.content.Context
import com.example.checkpill.model.PillInventoryRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PillInventoryStore(context: Context) {
    private val appContext = context.applicationContext
    private val dao = CheckPillDatabase.getInstance(appContext).pillInventoryDao()
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        migrateSharedPreferencesIfNeeded()
    }

    fun getRecords(): List<PillInventoryRecord> {
        return dao.getRecords()
    }

    fun getRecord(id: Long): PillInventoryRecord? = dao.getRecord(id)

    fun addRecord(record: PillInventoryRecord): Long {
        return dao.insert(record)
    }

    fun updateRecord(record: PillInventoryRecord) {
        dao.update(record)
    }

    fun deleteRecord(record: PillInventoryRecord) {
        dao.delete(record)
    }

    fun getTotalCount(): Int = getRecords().sumOf { it.signedCount() }

    fun getTotalsByPillName(): List<Pair<String, Int>> {
        return getRecords()
            .groupBy { it.pillName }
            .map { (name, records) -> name to records.sumOf { it.signedCount() } }
            .sortedBy { it.first }
    }

    fun getTotalCountForPill(pillName: String): Int {
        return getRecords()
            .filter { it.pillName == pillName }
            .sumOf { it.signedCount() }
    }

    private fun migrateSharedPreferencesIfNeeded() {
        if (prefs.getBoolean(KEY_MIGRATED_TO_ROOM, false)) {
            return
        }

        val legacyRecords = readLegacyRecords()
        legacyRecords.forEach { record ->
            dao.insert(record.copy(id = 0))
        }

        prefs.edit()
            .putBoolean(KEY_MIGRATED_TO_ROOM, true)
            .remove(KEY_RECORDS)
            .apply()
    }

    private fun readLegacyRecords(): List<PillInventoryRecord> {
        val json = prefs.getString(KEY_RECORDS, null) ?: return emptyList()
        val type = object : TypeToken<List<PillInventoryRecord>>() {}.type
        return runCatching {
            gson.fromJson<List<PillInventoryRecord>>(json, type)
        }.getOrDefault(emptyList())
    }

    companion object {
        private const val PREF_NAME = "pill_inventory"
        private const val KEY_RECORDS = "records"
        private const val KEY_MIGRATED_TO_ROOM = "migrated_to_room"
    }
}
