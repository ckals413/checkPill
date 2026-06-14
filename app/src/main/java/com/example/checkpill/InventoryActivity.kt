package com.example.checkpill

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.checkpill.adapter.PillInventoryAdapter
import com.example.checkpill.data.PillInventoryStore
import com.example.checkpill.databinding.ActivityInventoryBinding

class InventoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInventoryBinding
    private lateinit var store: PillInventoryStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        store = PillInventoryStore(this)

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        renderInventory()
    }

    private fun renderInventory() {
        val records = store.getRecords()
        val summary = store.getTotalsByPillName()

        binding.totalCountTextView.text = "전체 저장 수량 ${store.getTotalCount()}정"
        binding.summaryTextView.text = if (summary.isEmpty()) {
            "아직 저장된 재고가 없습니다."
        } else {
            summary.joinToString(separator = "\n") { (name, count) -> "$name: ${count}정" }
        }

        binding.emptyTextView.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
        binding.inventoryRecyclerView.visibility = if (records.isEmpty()) View.GONE else View.VISIBLE
        binding.inventoryRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.inventoryRecyclerView.adapter = PillInventoryAdapter(records)
    }
}
