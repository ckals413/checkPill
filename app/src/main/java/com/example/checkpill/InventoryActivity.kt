package com.example.checkpill

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.checkpill.adapter.PillInventoryAdapter
import com.example.checkpill.data.PillInventoryStore
import com.example.checkpill.databinding.ActivityInventoryBinding
import com.example.checkpill.model.PillInventoryRecord
import com.example.checkpill.model.PillInventoryRecord.Companion.TYPE_IN
import com.example.checkpill.model.PillInventoryRecord.Companion.TYPE_OUT
import com.example.checkpill.notification.LowStockNotifier

class InventoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInventoryBinding
    private lateinit var store: PillInventoryStore
    private lateinit var lowStockNotifier: LowStockNotifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        store = PillInventoryStore(this)
        lowStockNotifier = LowStockNotifier(this)

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
        binding.inventoryRecyclerView.adapter = PillInventoryAdapter(
            records = records,
            onEditClicked = { showEditDialog(it) },
            onDeleteClicked = { showDeleteDialog(it) }
        )
    }

    private fun showEditDialog(record: PillInventoryRecord) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
        }

        val nameEditText = EditText(this).apply {
            hint = "알약 이름"
            setText(record.pillName)
            maxLines = 1
        }
        val countEditText = EditText(this).apply {
            hint = "알약 개수"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(record.pillCount.toString())
            maxLines = 1
        }
        val typeGroup = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
        }
        val inboundButton = RadioButton(this).apply {
            text = "입고"
            id = View.generateViewId()
        }
        val outboundButton = RadioButton(this).apply {
            text = "출고"
            id = View.generateViewId()
        }
        typeGroup.addView(inboundButton)
        typeGroup.addView(outboundButton)
        typeGroup.check(if (record.transactionType == TYPE_OUT) outboundButton.id else inboundButton.id)
        val expirationEditText = EditText(this).apply {
            hint = "유통기한 예: 2026.12.31"
            setText(record.expirationDateText.orEmpty())
            maxLines = 1
        }
        container.addView(nameEditText)
        container.addView(countEditText)
        container.addView(typeGroup)
        container.addView(expirationEditText)

        AlertDialog.Builder(this)
            .setTitle("재고 기록 수정")
            .setView(container)
            .setNegativeButton("취소", null)
            .setPositiveButton("저장") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val count = countEditText.text.toString().toIntOrNull() ?: 0
                if (name.isBlank() || count <= 0) {
                    Toast.makeText(this, "이름과 개수를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val transactionType = if (typeGroup.checkedRadioButtonId == outboundButton.id) {
                    TYPE_OUT
                } else {
                    TYPE_IN
                }
                store.updateRecord(
                    record.copy(
                        pillName = name,
                        pillCount = count,
                        transactionType = transactionType,
                        expirationDateText = expirationEditText.text.toString().trim().ifBlank { null }
                    )
                )
                lowStockNotifier.notifyIfLowStock(name, store)
                Toast.makeText(this, "재고 기록이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                renderInventory()
            }
            .show()
    }

    private fun showDeleteDialog(record: PillInventoryRecord) {
        AlertDialog.Builder(this)
            .setTitle("재고 기록 삭제")
            .setMessage("${record.pillName} ${record.pillCount}정 기록을 삭제할까요?")
            .setNegativeButton("취소", null)
            .setPositiveButton("삭제") { _, _ ->
                store.deleteRecord(record)
                lowStockNotifier.notifyIfLowStock(record.pillName, store)
                Toast.makeText(this, "재고 기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                renderInventory()
            }
            .show()
    }
}
