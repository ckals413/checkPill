package com.example.checkpill.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.checkpill.databinding.ItemPillInventoryBinding
import com.example.checkpill.model.PillInventoryRecord

class PillInventoryAdapter(
    private val records: List<PillInventoryRecord>
) : RecyclerView.Adapter<PillInventoryAdapter.InventoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val binding = ItemPillInventoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InventoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount(): Int = records.size

    class InventoryViewHolder(
        private val binding: ItemPillInventoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: PillInventoryRecord) {
            binding.pillNameTextView.text = record.pillName
            binding.pillCountTextView.text = "${record.pillCount}정"
            binding.savedAtTextView.text = record.savedAtText
        }
    }
}
