package com.example.checkpill.sync

import android.content.Context
import com.example.checkpill.data.PillInventoryStore

class SyncManager(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getLastSyncText(): String = prefs.getString(KEY_LAST_SYNC_TEXT, "아직 동기화 기록이 없습니다.").orEmpty()

    fun syncNow(store: PillInventoryStore): SyncResult {
        val serverUrl = prefs.getString(KEY_SERVER_URL, "").orEmpty()
        if (serverUrl.isBlank()) {
            return SyncResult(
                success = false,
                message = "서버 URL이 설정되지 않아 로컬 기록 ${store.getRecords().size}건을 대기 상태로 유지합니다."
            )
        }

        val resultText = "서버 동기화 준비 완료: ${store.getRecords().size}건"
        prefs.edit()
            .putString(KEY_LAST_SYNC_TEXT, resultText)
            .apply()
        return SyncResult(success = true, message = resultText)
    }

    fun saveServerUrl(serverUrl: String) {
        prefs.edit()
            .putString(KEY_SERVER_URL, serverUrl)
            .apply()
    }

    fun getServerUrl(): String = prefs.getString(KEY_SERVER_URL, "").orEmpty()

    data class SyncResult(
        val success: Boolean,
        val message: String
    )

    companion object {
        private const val PREF_NAME = "sync_settings"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_LAST_SYNC_TEXT = "last_sync_text"
    }
}
