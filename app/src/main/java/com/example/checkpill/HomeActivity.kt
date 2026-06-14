package com.example.checkpill

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.checkpill.databinding.ActivityHomeBinding
import java.io.File
import java.io.FileOutputStream

class HomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding

    companion object {
        const val REQUEST_PERMISSIONS = 1001
    }

    // 버튼 클릭을 기록하는 변수
    private var clickedButton: String? = null
    private val takePicturePreviewLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap == null) {
            Toast.makeText(this, "사진 촬영이 취소되었습니다.", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        handleCapturedBitmap(bitmap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 초기 프래그먼트 설정
        replaceFragment(HomeFragment())

        // 바텀 네비게이션 설정
        setupBottomNavigation()

        // FAB 이벤트 설정
        setupSearchFab()

        // 권한 체크
        checkPermissions()
    }

    private fun setupBottomNavigation() {
        // 바텀 네비게이션 배경 제거
        binding.bottomNavigationView.background = null

        // 가운데 아이템 비활성화
        binding.bottomNavigationView.menu.getItem(1).isEnabled = false

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_home -> replaceFragment(HomeFragment())
                R.id.fragment_settings -> replaceFragment(SettingsFragment())
            }
            true
        }
    }

    private fun setupSearchFab() {
        binding.searchFab.setOnClickListener {
            startPillCount()
        }
    }

    // 프래그먼트 교체 함수
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // 약 검색 기능 시작
    fun startPillSearch() {
        clickedButton = "pillSearch"
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent()
        } else {
            requestCameraPermission()
        }
    }

    // 약 개수 세기 기능 시작
    fun startPillCount() {
        clickedButton = "pillNum"
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent()
        } else {
            requestCameraPermission()
        }
    }

    private fun checkPermissions() {
        val permissions = buildRequiredPermissions()
        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermissions(permissions, REQUEST_PERMISSIONS)
        }
    }

    private fun requestCameraPermission() {
        val permissions = buildRequiredPermissions()
        requestPermissions(permissions, REQUEST_PERMISSIONS)
    }

    private fun buildRequiredPermissions(): Array<String> {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return permissions.toTypedArray()
    }

    private fun dispatchTakePictureIntent() {
        try {
            takePicturePreviewLauncher.launch(null)
        } catch (e: Exception) {
            Log.e("HomeActivity", "카메라 실행 실패: ${e.message}")
            Toast.makeText(this, "카메라 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): Uri? {
        return try {
            val file = File(cacheDir, "captured_image.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun handleCapturedBitmap(bitmap: Bitmap) {
        try {
            Log.d("HomeActivity", "이미지 로드 성공")
            val imageBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, false)
            val fileUri = saveBitmapToFile(imageBitmap)
            fileUri?.let {
                Log.d("HomeActivity", "이미지 파일 저장 성공: $fileUri")
                when (clickedButton) {
                    "pillNum" -> {
                        val intent = Intent(this, ResultPillNumActivity::class.java)
                        intent.putExtra("imageUri", fileUri.toString())
                        startActivity(intent)
                    }
                    "pillSearch" -> {
                        val intent = Intent(this, ResultPillSearchCameraActivity::class.java)
                        intent.putExtra("imageUri", fileUri.toString())
                        startActivity(intent)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("HomeActivity", "이미지 처리 중 오류 발생: ${e.message}")
            Toast.makeText(this, "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
