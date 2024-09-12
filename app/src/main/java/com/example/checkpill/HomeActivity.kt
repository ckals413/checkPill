package com.example.checkpill

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.checkpill.databinding.ActivityHomeBinding

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
            clickedButton = "pillNum" // 어떤 버튼이 클릭되었는지 기록
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                requestCameraPermission()
            }
        }

        // homeSearchCV 클릭 시 ResultPillSearchActivity로 이동
        binding.homeSearchCV.setOnClickListener {
            clickedButton = "pillSearch" // 어떤 버튼이 클릭되었는지 기록
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
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                dispatchTakePictureIntent()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            val imageBitmap: Bitmap? = if (imageUri == null) {
                val extras = data.extras
                extras?.get("data") as? Bitmap
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            }

            imageBitmap?.let {
                // 클릭된 버튼에 따라 다른 액티비티로 전환
                when (clickedButton) {
                    "pillNum" -> {
                        val intent = Intent(this, ResultPillNumActivity::class.java)
                        intent.putExtra("imageBitmap", it)
                        startActivity(intent)
                    }
                    "pillSearch" -> {
                        val intent = Intent(this, ResultPillSearchActivity::class.java)
                        intent.putExtra("imageBitmap", it)
                        startActivity(intent)

                    }
                }
            }
        }
    }
}
