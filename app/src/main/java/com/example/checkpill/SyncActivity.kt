package com.example.checkpill

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.checkpill.data.PillInventoryStore
import com.example.checkpill.databinding.ActivitySyncBinding
import com.example.checkpill.sync.SyncManager

class SyncActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySyncBinding
    private lateinit var store: PillInventoryStore
    private lateinit var syncManager: SyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncBinding.inflate(layoutInflater)
        setContentView(binding.root)

        store = PillInventoryStore(this)
        syncManager = SyncManager(this)

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.serverUrlEditText.setText(syncManager.getServerUrl())
        renderSyncState()

        binding.saveServerButton.setOnClickListener {
            syncManager.saveServerUrl(binding.serverUrlEditText.text.toString().trim())
            Toast.makeText(this, "서버 설정이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            renderSyncState()
        }

        binding.syncNowButton.setOnClickListener {
            val result = syncManager.syncNow(store)
            Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
            renderSyncState(result.message)
        }
    }

    private fun renderSyncState(message: String = syncManager.getLastSyncText()) {
        binding.localRecordCountTextView.text = "로컬 재고 기록 ${store.getRecords().size}건"
        binding.lastSyncTextView.text = message
    }
}
