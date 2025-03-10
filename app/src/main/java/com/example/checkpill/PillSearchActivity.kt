package com.example.checkpill

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.checkpill.adapter.PillSearchAdapter
import com.example.checkpill.databinding.ActivityPillSearchBinding
import com.example.checkpill.model.PillInfo
import com.example.checkpill.network.PillApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PillSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPillSearchBinding
    private lateinit var adapter: PillSearchAdapter
    private val apiClient = PillApiClient()
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPillSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchView()
        setupBackButton()
    }

    private fun setupRecyclerView() {
        adapter = PillSearchAdapter { pillInfo ->
            // 알약 상세 정보 화면으로 이동
            navigateToPillDetail(pillInfo)
        }

        binding.pillRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PillSearchActivity)
            adapter = this@PillSearchActivity.adapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    searchPills(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 입력할 때마다 검색하지 않고 제출 시에만 검색
                return false
            }
        })
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun searchPills(query: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyResultText.visibility = View.GONE


        searchJob?.cancel()

        searchJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiClient.searchPills(query)

                // 메인 스레드로 돌아오기 전에 비활성화된 경우 처리 중단
                if (!isActive) return@launch

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val pillList = response.body()?.body?.items ?: emptyList()

                        // 리스트가 비어있지 않으면 처리
                        if (pillList.isNotEmpty()) {
                            adapter.submitList(null) // 이전 목록 초기화
                            adapter.submitList(pillList)
                        } else {
                            binding.emptyResultText.visibility = View.VISIBLE
                        }
                    } else {
                        showErrorMessage("데이터를 불러오는데 실패했습니다. (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                // 취소된 작업은 오류 메시지 표시하지 않음
                if (isActive) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        showErrorMessage("네트워크 오류가 발생했습니다: ${e.message}")
                    }
                }
            }
        }
    }

    private fun navigateToPillDetail(pillInfo: PillInfo) {
        val intent = Intent(this, PillDetailActivity::class.java)
        intent.putExtra("pillInfo", pillInfo)
        startActivity(intent)
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // 액티비티 파괴 시 작업 취소
    override fun onDestroy() {
        super.onDestroy()
        searchJob?.cancel()
    }

}