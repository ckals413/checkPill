package com.example.checkpill.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.example.checkpill.databinding.ItemPillInventoryBinding
import com.example.checkpill.model.PillInventoryRecord

class PillInventoryAdapter(
    private val records: List<PillInventoryRecord>,
    private val onEditClicked: (PillInventoryRecord) -> Unit,
    private val onDeleteClicked: (PillInventoryRecord) -> Unit
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
        holder.bind(records[position], onEditClicked, onDeleteClicked)
    }

    override fun getItemCount(): Int = records.size

    class InventoryViewHolder(
        private val binding: ItemPillInventoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            record: PillInventoryRecord,
            onEditClicked: (PillInventoryRecord) -> Unit,
            onDeleteClicked: (PillInventoryRecord) -> Unit
        ) {
            binding.pillNameTextView.text = record.pillName
            binding.pillCountTextView.text = "${record.transactionLabel()} ${record.pillCount}정"
            binding.savedAtTextView.text = record.savedAtText
            binding.extraInfoTextView.text = record.expirationDateText?.let {
                "유통기한 $it"
            } ?: "유통기한 미입력"
            record.photoUri?.let {
                binding.pillPhotoImageView.setImageURI(Uri.parse(it))
            } ?: run {
                binding.pillPhotoImageView.setImageResource(com.example.checkpill.R.drawable.ic_pill_placeholder)
            }
            binding.editButton.setOnClickListener {
                onEditClicked(record)
            }
            binding.deleteButton.setOnClickListener {
                onDeleteClicked(record)
            }
        }
    }
}
