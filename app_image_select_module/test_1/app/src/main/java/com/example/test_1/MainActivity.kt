package com.example.test_1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.test_1.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var imageView: ImageView
    private var currentPhotoPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        val takePictureButton: Button = findViewById(R.id.takePictureButton)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            takePictureButton.setOnClickListener {
                dispatchTakePictureIntent()
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                showPermissionDeniedMessage()
            }
        }
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(this, "Camera permission is required to take a picture", Toast.LENGTH_LONG).show()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.resolveActivity(packageManager)?.also {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            photoFile?.let {
                val photoURI: Uri = FileProvider.getUriForFile(this, "com.example.cameraapp.fileprovider", it)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        currentPhotoPath = image.absolutePath
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imgFile = File(currentPhotoPath)
            if (imgFile.exists()) {
                imageView.setImageURI(Uri.fromFile(imgFile))

                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                uploadImage(bitmap)
            }
        }
    }

    private fun uploadImage(bitmap: Bitmap) {
        val client = OkHttpClient()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        val mediaType = "image/jpeg".toMediaType()
        val requestBody: RequestBody = byteArray.toRequestBody(mediaType)

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "image.jpg", requestBody)
            .build()

        val request = Request.Builder()
            .url("YOUR_SERVER_URL_HERE")
            .post(multipartBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                println("Image uploaded successfully")
            } else {
                println("Image upload failed: ${response.message}")
            }
        }
    }
}
