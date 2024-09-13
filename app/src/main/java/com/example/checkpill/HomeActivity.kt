package com.example.checkpill

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.checkpill.databinding.ActivityHomeBinding
import java.io.File
import java.io.FileOutputStream

class HomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding

    companion object {
        const val REQUEST_PERMISSIONS = 1001
        const val REQUEST_IMAGE_CAPTURE = 1002
    }

    // 버튼 클릭을 기록하는 변수
    private var clickedButton: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // homePillNumCV 클릭 시 ResultPillNumActivity로 이동
        binding.homePillNumCV.setOnClickListener {
            clickedButton = "pillNum"
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                requestCameraPermission()
            }
        }

        // homeSearchCV 클릭 시 ResultPillSearchActivity로 이동
        binding.homeSearchCV.setOnClickListener {
            clickedButton = "pillSearch"
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                requestCameraPermission()
            }
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermissions(permissions, REQUEST_PERMISSIONS)
        }
    }

    private fun requestCameraPermission() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        requestPermissions(permissions, REQUEST_PERMISSIONS)
    }

    // 하나의 메서드로 카메라 인텐트 처리
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(this, "카메라 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 비트맵을 파일로 저장하는 함수
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            var imageBitmap: Bitmap? = null

            try {
                imageBitmap = if (imageUri == null) {
                    val extras = data.extras
                    extras?.get("data") as? Bitmap
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                }

                imageBitmap?.let {
                    Log.d("HomeActivity", "이미지 로드 성공")
                    imageBitmap = Bitmap.createScaledBitmap(it, 640, 640, false)

                    val fileUri = saveBitmapToFile(imageBitmap!!)
                    fileUri?.let {
                        Log.d("HomeActivity", "이미지 파일 저장 성공: $fileUri")
                        when (clickedButton) {
                            "pillNum" -> {
                                val intent = Intent(this, ResultPillNumActivity::class.java)
                                intent.putExtra("imageUri", fileUri.toString())
                                startActivity(intent)
                            }
                            "pillSearch" -> {
                                val intent = Intent(this, ResultPillSearchActivity::class.java)
                                intent.putExtra("imageUri", fileUri.toString())
                                startActivity(intent)
                            }
                        }
                    }
                } ?: run {
                    Log.e("HomeActivity", "이미지 로드 실패")
                    Toast.makeText(this, "사진을 가져오는 데 문제가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("HomeActivity", "이미지 처리 중 오류 발생: ${e.message}")
                Toast.makeText(this, "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
