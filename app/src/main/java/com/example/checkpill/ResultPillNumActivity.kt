package com.example.checkpill

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.checkpill.databinding.ActivityResultPillNumBinding
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ResultPillNumActivity : AppCompatActivity() {
    lateinit var binding: ActivityResultPillNumBinding
    private lateinit var tflite: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultPillNumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // YOLO 모델 로딩
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
            Log.d("ResultPillNumActivity", "모델 로딩 완료")
        } catch (e: Exception) {
            Log.e("ResultPillNumActivity", "모델 로딩 실패: ${e.message}")
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
        val output = Array(1) { Array(25200) { FloatArray(14) } }

        // 추론 실행
        try {
            tflite.run(inputBuffer, output)
            Log.d("ResultPillNumActivity", "추론 성공")
        } catch (e: Exception) {
            Log.e("ResultPillNumActivity", "추론 실패: ${e.message}")
        }

        // 새로운 중앙점 기반 알약 개수 계산 함수 호출
        return processOutputWithCenters(output)
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

    // 중앙점을 기반으로 알약 개수를 계산하는 함수
    private fun processOutputWithCenters(output: Array<Array<FloatArray>>): Int {
        val confidenceThreshold = 0.6f  // 신뢰도 임계값
        val minDistance = 0.05f         // 최소 거리 임계값
        val centers = mutableListOf<Pair<Float, Float>>()

        // 각 탐지 결과에서 중앙점 계산
        for (detection in output[0]) {
            val confidence = detection[4]
            if (confidence > confidenceThreshold) {
                val centerX = (detection[0] + detection[2]) / 2
                val centerY = (detection[1] + detection[3]) / 2
                centers.add(Pair(centerX, centerY))

                Log.d("ResultPillNumActivity", "Detection center: ($centerX, $centerY)")
            }
        }

        // 중복된 중앙점 제거
        val uniqueCenters = removeDuplicateCenters(centers, minDistance)

        // 최종 알약 개수
        Log.d("ResultPillNumActivity", "Final pill count after duplicate center removal = ${uniqueCenters.size}")
        return uniqueCenters.size
    }

    // 중복된 중앙점을 제거하는 함수
    private fun removeDuplicateCenters(centers: MutableList<Pair<Float, Float>>, minDistance: Float): List<Pair<Float, Float>> {
        val uniqueCenters = mutableListOf<Pair<Float, Float>>()

        for (center in centers) {
            var isUnique = true
            for (uniqueCenter in uniqueCenters) {
                val distance = Math.sqrt(Math.pow((center.first - uniqueCenter.first).toDouble(), 2.0) + Math.pow((center.second - uniqueCenter.second).toDouble(), 2.0))
                if (distance < minDistance) {
                    isUnique = false
                    break
                }
            }
            if (isUnique) {
                uniqueCenters.add(center)
            }
        }
        return uniqueCenters
    }
}
