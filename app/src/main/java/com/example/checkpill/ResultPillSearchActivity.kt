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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.checkpill.databinding.ActivityResultPillSearchBinding
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class ResultPillSearchActivity : AppCompatActivity() {
    lateinit var binding: ActivityResultPillSearchBinding
    private lateinit var tflite: Interpreter

    private val REQUEST_PERMISSIONS = 1
    private val MODEL_PATH = "best-fp16.tflite"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultPillSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadModel()
        checkPermissions()

        val imageUri = intent.getStringExtra("imageUri")
        imageUri?.let {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(it))
                Log.d("ResultPillSearchActivity!", "이미지 로드 성공: $imageUri")
                binding.medicineIv.setImageBitmap(bitmap)
                runInference(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ResultPillSearchActivity!", "이미지 로드 중 오류 발생: ${e.message}")
                Toast.makeText(this, "이미지 로드 실패", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Log.e("ResultPillSearchActivity!", "이미지 경로 없음")
            Toast.makeText(this, "이미지 로드 실패", Toast.LENGTH_SHORT).show()
        }

        binding.backButton.setOnClickListener { onBackPressed() }
        binding.checkCombinationButton.setOnClickListener { finish() }
    }

    private fun loadModel() {
        try {
            val assetManager = assets
            val modelFileDescriptor = assetManager.openFd(MODEL_PATH)
            val inputStream = FileInputStream(modelFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = modelFileDescriptor.startOffset
            val declaredLength = modelFileDescriptor.declaredLength
            val modelByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            tflite = Interpreter(modelByteBuffer)
            Log.d("ResultPillSearchActivity!", "모델 로드 성공")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ResultPillSearchActivity!", "모델 로드 중 오류 발생: ${e.message}")
            Toast.makeText(this, "모델 로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val inputSize = 640
        val byteBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false)

        val intValues = IntArray(inputSize * inputSize)
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

        var pixel = 0
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val value = intValues[pixel++]
                byteBuffer.putFloat(((value shr 16) and 0xFF) / 255.0f)
                byteBuffer.putFloat(((value shr 8) and 0xFF) / 255.0f)
                byteBuffer.putFloat((value and 0xFF) / 255.0f)
            }
        }
        return byteBuffer
    }

    private fun runInference(bitmap: Bitmap) {
        val input = convertBitmapToByteBuffer(bitmap)
        val output = Array(1) { Array(25200) { FloatArray(16) } }

        try {
            tflite.run(input, output)
            // 모델 출력을 확인하는 로그
            for (detection in output[0]) {
                Log.d("ModelOutput", "Detection: ${detection.contentToString()}")
            }
            processResult(output)
            Log.d("ResultPillSearchActivity!", "모델 추론 성공")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ResultPillSearchActivity!", "모델 추론 중 오류 발생: ${e.message}")
        }
    }


    // 클래스 인덱스를 알약 이름과 매핑하는 Map 생성
    val pillClasses = mapOf(
        0 to "A",
        1 to "B",
        2 to "C",
        3 to "D",
        4 to "E",
        5 to "F",
        6 to "G",
        7 to "H",
        8 to "I",
        9 to "J",
        10 to "K"
    )

    private fun processResult(output: Array<Array<FloatArray>>) {
        val detectedObjects = ArrayList<String>()

        for (detection in output[0]) {
            val confidence = detection[4] // 객체의 신뢰도
            if (confidence > 0.6) {  // 임계값 0.5 이상일 경우
                // 탐지된 객체의 클래스 확률을 추출 (detection[5] 이후는 클래스 확률)
                val classProbabilities = detection.copyOfRange(5, detection.size)
                val classIndex = classProbabilities.indices.maxByOrNull { classProbabilities[it] } ?: 0
                val pillName = pillClasses[classIndex] ?: "Unknown"  // 클래스 인덱스를 이름으로 변환

                Log.d("ClassIndex", "Detected class index: $classIndex, name: $pillName")
                detectedObjects.add("Detected pill: $pillName with confidence $confidence")
            }
        }

        // 탐지된 결과를 화면에 표시
        runOnUiThread {
            binding.detectResultTV.text = detectedObjects.joinToString("\n")
        }
    }




    private fun checkPermissions() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS)
        }
    }
}
