package com.example.checkpill.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.checkpill.R
import com.example.checkpill.databinding.ItemPillBinding
import com.example.checkpill.model.PillInfo

/**
 * 알약 검색 결과 리사이클러뷰 어댑터
 */
class PillSearchAdapter(
    private val onItemClick: (PillInfo) -> Unit
) : ListAdapter<PillInfo, PillSearchAdapter.PillViewHolder>(PillDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PillViewHolder {
        val binding = ItemPillBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PillViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PillViewHolder(
        private val binding: ItemPillBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(pill: PillInfo) {
            binding.apply {
                // 알약 이름 설정
                pillNameTextView.text = pill.itemName

                // 제조사 정보 설정
                pillCompanyTextView.text = "제조사: ${pill.entpName}"

                // 전문/일반 구분 표시
                val pillClass = pill.etcOtcName ?: "미분류"
                pillClassTextView.text = pillClass

                // 전문/일반 구분에 따라 배경색 변경
                val backgroundResource = when {
                    pillClass.contains("전문") -> R.drawable.pill_class_bg_prescription // 전문의약품용 배경
                    pillClass.contains("일반") -> R.drawable.pill_class_background // 일반의약품용 배경
                    else -> R.drawable.pill_class_background // 기본 배경
                }
                pillClassTextView.setBackgroundResource(backgroundResource)

                // 이미지 로딩
                if (!pill.itemImage.isNullOrEmpty()) {
                    Glide.with(pillImageView.context)
                        .load(pill.itemImage)
                        .placeholder(R.drawable.ic_pill_placeholder)
                        .error(R.drawable.ic_pill_placeholder)
                        .into(pillImageView)
                } else {
                    // 이미지가 없는 경우 기본 이미지 표시
                    pillImageView.setImageResource(R.drawable.ic_pill_placeholder)
                }
            }
        }
    }
}

/**
 * 알약 아이템 비교를 위한 DiffUtil 콜백
 */
class PillDiffCallback : DiffUtil.ItemCallback<PillInfo>() {
    override fun areItemsTheSame(oldItem: PillInfo, newItem: PillInfo): Boolean {
        return oldItem.itemSeq == newItem.itemSeq
    }

    override fun areContentsTheSame(oldItem: PillInfo, newItem: PillInfo): Boolean {
        return oldItem == newItem
    }
}