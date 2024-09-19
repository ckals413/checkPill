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
        val output = Array(1) { Array(25200) { FloatArray(16) } }

        // 추론 실행
        try {
            tflite.run(inputBuffer, output)
            Log.d("ResultPillNumActivity", "추론 성공")
        } catch (e: Exception) {
            Log.e("ResultPillNumActivity", "추론 실패: ${e.message}")
        }

        // NMS를 적용하여 알약 개수를 계산
        val pillCount = processOutputWithNMS(output)

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

    // NMS 적용하여 추론 결과 처리 및 알약 개수 계산
    private fun processOutputWithNMS(output: Array<Array<FloatArray>>): Int {
        val confidenceThreshold = 0.6f  // Confidence threshold for counting detections
        val iouThreshold = 0.5f         // IoU threshold for NMS

        val boxes = mutableListOf<RectF>()
        val scores = mutableListOf<Float>()

        for (i in output[0].indices) {
            val detection = output[0][i]
            val confidence = detection[4]
            if (confidence > confidenceThreshold) {
                // 검출된 바운딩 박스와 confidence 값을 저장
                val box = RectF(detection[0], detection[1], detection[2], detection[3])
                boxes.add(box)
                scores.add(confidence)

                Log.d("ResultPillNumActivity2", "Detection $i: Confidence = $confidence, Box = $box")
            }
        }

        // 좌표 차이와 IoU를 함께 고려한 병합
        val finalBoxes = mergeSimilarBoxesWithIoU(boxes, scores, iouThreshold)

        // 최종 알약 개수
        Log.d("ResultPillNumActivity3", "Final pill count after NMS = ${finalBoxes.size}")
        return finalBoxes.size
    }

    // 박스 좌표 간 차이가 ±2 이하인지 확인하는 함수
    private fun isSimilarBox(boxA: RectF, boxB: RectF): Boolean {
        return (abs(boxA.left - boxB.left) < 0.4f &&
                abs(boxA.top - boxB.top) < 0.4f &&
                abs(boxA.right - boxB.right) < 0.4f &&
                abs(boxA.bottom - boxB.bottom) < 0.4f)
    }

    // IoU 계산 함수
    private fun iou(boxA: RectF, boxB: RectF): Float {
        val intersectionLeft = max(boxA.left, boxB.left)
        val intersectionTop = max(boxA.top, boxB.top)
        val intersectionRight = min(boxA.right, boxB.right)
        val intersectionBottom = min(boxA.bottom, boxB.bottom)

        val intersectionArea = max(0f, intersectionRight - intersectionLeft) * max(0f, intersectionBottom - intersectionTop)

        val boxAArea = (boxA.right - boxA.left) * (boxA.bottom - boxA.top)
        val boxBArea = (boxB.right - boxB.left) * (boxB.bottom - boxB.top)

        return intersectionArea / (boxAArea + boxBArea - intersectionArea)
    }

    // 좌표 차이 및 IoU를 모두 고려한 병합 함수
    private fun mergeSimilarBoxesWithIoU(boxes: List<RectF>, scores: List<Float>, iouThreshold: Float): List<RectF> {
        val mergedBoxes = mutableListOf<RectF>()

        for (box in boxes) {
            var isMerged = false
            for (mergedBox in mergedBoxes) {
                if (isSimilarBox(mergedBox, box) || iou(mergedBox, box) > iouThreshold) {
                    // 유사한 박스는 병합
                    mergedBox.left = (mergedBox.left + box.left) / 2
                    mergedBox.top = (mergedBox.top + box.top) / 2
                    mergedBox.right = (mergedBox.right + box.right) / 2
                    mergedBox.bottom = (mergedBox.bottom + box.bottom) / 2
                    isMerged = true
                    break
                }
            }
            if (!isMerged) {
                mergedBoxes.add(RectF(box))
            }
        }

        return mergedBoxes
    }
}
