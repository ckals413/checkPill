package com.example.checkpill

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.checkpill.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 액티비티에서 CardView 클릭 이벤트 핸들러 로직을 가져옴
        // 약 검색 카드 클릭 이벤트
        binding.homeSearchCV.setOnClickListener {
            context?.let {
                val intent = Intent(it, PillSearchActivity::class.java)
                startActivity(intent)
            }
        }

        // 약 개수 카드 클릭 이벤트
        binding.homePillNumCV.setOnClickListener {
            (activity as? HomeActivity)?.startPillCount()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}