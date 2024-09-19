package com.example.checkpill

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.example.checkpill.databinding.ActivityResultPillNumBinding
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class ResultPillNumActivity : AppCompatActivity() {
    lateinit var binding: ActivityResultPillNumBinding
    private lateinit var tflite: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultPillNumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load the YOLO model (TFLite)
        loadModel()

        // 이미지 URI를 받아서 이미지 표시
        val imageUri = intent.getStringExtra("imageUri")
        imageUri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(it))
            binding.medicineIv.setImageBitmap(bitmap)

            // YOLO 모델을 사용하여 알약 개수 계산
            val pillCount = detectPills(bitmap)

            // 인식된 알약 개수를 표시
            binding.detectResultNumTV.text = "인식된 알약의 개수: $pillCount"
        }

        // 확인 버튼
        binding.checkCombinationButton.setOnClickListener {
            finish()
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    // YOLO 모델 로딩 함수
    private fun loadModel() {
        try {
            // TFLite 모델 파일 로드
            val modelFile = assets.openFd("best-fp16.tflite")
            val inputStream = FileInputStream(modelFile.fileDescriptor)
            val modelBuffer = inputStream.channel.map(FileChannel.MapMode.READ_ONLY, modelFile.startOffset, modelFile.declaredLength)
            tflite = Interpreter(modelBuffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // YOLO 모델을 사용하여 알약을 인식하는 함수
    private fun detectPills(bitmap: Bitmap): Int {
        // 이미지를 YOLO 모델 입력 형식에 맞게 변환
        val inputSize = 640 // 모델이 학습된 입력 크기
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false)
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

        // YOLO 모델 추론
        val output = Array(1) { Array(25200) { FloatArray(16) } }

        // 추론을 실행
        tflite.run(inputBuffer, output)

        // 알약 개수 계산 (confidence 값이 일정 수준 이상인 박스만 선택)
        val pillCount = processOutput(output)

        return pillCount
    }

    // 비트맵을 ByteBuffer로 변환하는 함수
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val inputSize = 640
        val byteBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputSize * inputSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (i in intValues.indices) {
            val value = intValues[i]
            byteBuffer.putFloat(((value shr 16) and 0xFF) / 255.0f) // Red
            byteBuffer.putFloat(((value shr 8) and 0xFF) / 255.0f)  // Green
            byteBuffer.putFloat((value and 0xFF) / 255.0f)          // Blue
        }
        return byteBuffer
    }

    // 추론 결과 처리 및 알약 개수 계산
    private fun processOutput(output: Array<Array<FloatArray>>): Int {
        var pillCount = 0
        val confidenceThreshold = 0.6f  // Confidence threshold for counting detections

        for (detection in output[0]) {
            val confidence = detection[4]
            if (confidence > confidenceThreshold) {
                // 알약으로 인식된 개수 증가
                pillCount++
            }
        }
        return pillCount
    }
}