package com.example.checkpill

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.checkpill.databinding.ActivityResultPillNumBinding
import java.io.File
import java.io.FileOutputStream

class ResultPillNumActivity : AppCompatActivity() {
    lateinit var binding: ActivityResultPillNumBinding
    private var selectedMedicineName: String? = null

    private val REQUEST_PERMISSIONS = 1
    private val REQUEST_IMAGE_CAPTURE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultPillNumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check for permissions and set up listeners
        checkPermissions()

        val imageBitmap = intent.getParcelableExtra<Bitmap>("imageBitmap")
        imageBitmap?.let {
            binding.medicineIv.setImageBitmap(it)

            // Save the bitmap as a PNG file and upload it
            val pngFile = saveBitmapAsPng(it)
            //uploadImageToServer(pngFile)
        }

        binding.medicineIv.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSIONS)
            }
        }

        binding.checkCombinationButton.setOnClickListener {
//            val bottomSheetFragment3 = BottomSheetFragment3()
//            val fileUri = Uri.fromFile(File(cacheDir, "captured_image.png"))
//            val bundle = Bundle()
//            bundle.putString("photoUri", fileUri.toString())
//            bottomSheetFragment3.arguments = bundle
//            bottomSheetFragment3.show(supportFragmentManager, "BottomSheetDialog3")

        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS)
        }
    }

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
            val imageBitmap: Bitmap? = data.extras?.get("data") as? Bitmap

            imageBitmap?.let {
                binding.medicineIv.setImageBitmap(it)

                // Save the bitmap as a PNG file and upload it
                val pngFile = saveBitmapAsPng(it)
                //uploadImageToServer(pngFile)
            }
        }
    }

    private fun saveBitmapAsPng(bitmap: Bitmap): File {
        val pngFile = File(cacheDir, "captured_image.png")
        FileOutputStream(pngFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return pngFile
    }

//    private fun uploadImageToServer(file: File) {
//        val requestFile = RequestBody.create("image/png".toMediaTypeOrNull(), file)
//        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
//
//        val retrofit = getRetrofit()
//        val service = retrofit.create(CameraService::class.java)
//        val call = service.uploadImage(body)
//
//        call.enqueue(object : Callback<List<CameraMedicineResponse>> {
//            override fun onResponse(call: Call<List<CameraMedicineResponse>>, response: ImageProcessor.Response<List<CameraMedicineResponse>>) {
//                if (response.isSuccessful) {
//                    val cameraMedicineResponses = response.body()
//
//                    val medicines = cameraMedicineResponses?.map { response ->
//                        MedicineList(
//                            imageResId = response.imageUrl,
//                            name = response.name,
//                            dosage = response.dosage ?: "",
//                            effects = response.benefit ?: "",
//                            howToEat = response.dosage ?: ""
//                        )
//                    } ?: emptyList()
//
//                    selectedMedicineName = cameraMedicineResponses?.firstOrNull {
//                        it.name.contains("타이레놀") ||
//                                it.name.contains("초당아스피린장용정") ||
//                                it.name.contains("페니라민정")
//                    }?.name
//
//                    val listAdapter = MedicineListAdapter(medicines)
//                    binding.medicineInfoRecyclerView.adapter = listAdapter
//                } else {
//                    Toast.makeText(this@SearchResultActivity, "Error: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<List<CameraMedicineResponse>>, t: Throwable) {
//                Toast.makeText(this@SearchResultActivity, "Request failed: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
}

