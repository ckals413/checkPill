package com.example.checkpill

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.checkpill.databinding.ActivityPillDetailBinding
import com.example.checkpill.model.PillInfo

class PillDetailActivity  : AppCompatActivity() {

    private lateinit var binding: ActivityPillDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPillDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 이전 화면에서 전달받은 PillInfo 데이터 가져오기
        val pillInfo = intent.getParcelableExtra<PillInfo>("pillInfo")

        // 뒤로가기 버튼 설정
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        // 확인 버튼 설정
        binding.checkButton.setOnClickListener {
            finish()
        }

        // PillInfo가 있는 경우에만 UI 업데이트
        pillInfo?.let {
            setupUI(it)
        } ?: run {
            // 데이터가 없는 경우 에러 메시지 표시
            binding.pillNameTextView.text = "약품 정보를 불러올 수 없습니다."
        }
    }

    private fun setupUI(pillInfo: PillInfo) {
        // 약품 이름 설정
        binding.pillNameTextView.text = pillInfo.itemName

        // 제조사 설정
        binding.companyNameTextView.text = pillInfo.entpName

        // 의약품 일련번호
        binding.pillIdTextView.text = "의약품 일련번호: ${pillInfo.itemSeq}"

        // 이미지 로드
        if (!pillInfo.itemImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(pillInfo.itemImage)
                .placeholder(R.drawable.ic_pill_placeholder)
                .error(R.drawable.ic_pill_placeholder)
                .into(binding.pillImageView)
        } else {
            binding.pillImageView.setImageResource(R.drawable.ic_pill_placeholder)
        }

        // 효능 및 효과
        setTextWithTitle(binding.efficacyTextView, "효능 및 효과 (Efficacy)", pillInfo.efficacy)

        // 사용 방법
        setTextWithTitle(binding.usageTextView, "사용 방법", pillInfo.useMethod)

        // 주의사항 (모든 주의사항 통합)
        val warningBuilder = StringBuilder()

        // 주의사항 경고
        if (!pillInfo.atpnWarn.isNullOrEmpty()) {
            warningBuilder.append(formatText(pillInfo.atpnWarn))
        }

        // 일반 주의사항
        if (!pillInfo.atpn.isNullOrEmpty()) {
            if (warningBuilder.isNotEmpty()) warningBuilder.append("\n\n")
            warningBuilder.append(formatText(pillInfo.atpn))
        }

        // 상호작용
        if (!pillInfo.interaction.isNullOrEmpty()) {
            if (warningBuilder.isNotEmpty()) warningBuilder.append("\n\n")
            warningBuilder.append(formatText(pillInfo.interaction))
        }

        // 부작용
        if (!pillInfo.sideEffect.isNullOrEmpty()) {
            if (warningBuilder.isNotEmpty()) warningBuilder.append("\n\n")
            warningBuilder.append(formatText(pillInfo.sideEffect))
        }

        // 보관법
        if (!pillInfo.depositMethod.isNullOrEmpty()) {
            if (warningBuilder.isNotEmpty()) warningBuilder.append("\n\n")
            warningBuilder.append("【보관법】\n")
            warningBuilder.append(formatText(pillInfo.depositMethod))
        }

        // 통합된 주의사항 설정
        setTextWithTitle(binding.warningsTextView, "주의사항", warningBuilder.toString())

        // 제조사 사업자 등록번호
        if (!pillInfo.itemSeq.isNullOrEmpty()) {
            binding.bizrnoTextView.text = "제조사 사업자등록번호: ${pillInfo.itemSeq}"
            binding.bizrnoTextView.visibility = View.VISIBLE
        } else {
            binding.bizrnoTextView.visibility = View.GONE
        }
    }

    private fun setTextWithTitle(view: View, title: String, content: String?) {
        if (content.isNullOrEmpty()) {
            view.visibility = View.GONE
            return
        }

        view.visibility = View.VISIBLE
        if (view is android.widget.TextView) {
            val formattedText = "<b>$title</b><br/>${formatText(content)}"
            view.text = Html.fromHtml(formattedText, Html.FROM_HTML_MODE_COMPACT)
        }
    }

    // \n을 <br>로 변환하여 줄바꿈 처리
    private fun formatText(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        return text.replace("\n", "<br/>")
    }
}