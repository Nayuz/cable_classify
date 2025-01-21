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
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var uploadButton: Button  // 업로드 버튼 변수 선언
    // 카메라 권한 요청 코드
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    // 이미지 캡처 요청 코드
    private val REQUEST_IMAGE_CAPTURE = 1
    // 이미지뷰를 표시할 변수
    private lateinit var imageView: ImageView
    // 촬영한 사진의 경로를 저장할 변수
    private var currentPhotoPath: String = ""
    private var bitmap: Bitmap? = null // 비트맵을 전역 변수로 저장

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 레이아웃에서 이미지뷰와 버튼 초기화
        imageView = findViewById(R.id.imageView)
        val takePictureButton: Button = findViewById(R.id.takePictureButton)
        uploadButton = findViewById(R.id.uploadButton) // 새로 추가된 버튼

        // 초기 상태에서 업로드 버튼 숨기기
        uploadButton.visibility = View.GONE

        // 카메라 권한이 부여되어 있는지 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없다면 요청
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            // 권한이 있다면 버튼 클릭 시 사진 촬영 시작
            takePictureButton.setOnClickListener {
                dispatchTakePictureIntent()  // 사진 촬영 인텐트 시작
            }
        }
        // 업로드 버튼 클릭 리스너
        uploadButton.setOnClickListener {
            if (bitmap != null) {
                uploadImage(bitmap!!)  // 비트맵이 있을 경우 업로드
            } else {
                Toast.makeText(this, "No image to upload", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 승인되었으면 사진 촬영 시작
                dispatchTakePictureIntent()
            } else {
                // 권한이 거부되었으면 메시지 표시
                showPermissionDeniedMessage()
            }
        }
    }

    // 권한 거부 시 사용자에게 알림 메시지 표시
    private fun showPermissionDeniedMessage() {
        Toast.makeText(this, "Camera permission is required to take a picture", Toast.LENGTH_LONG).show()
    }

    // 카메라 촬영 인텐트를 시작하는 메서드
    private fun dispatchTakePictureIntent() {
        // 카메라로 이미지를 찍을 수 있는 인텐트 생성
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // 카메라 앱이 있을 경우에만 실행
        takePictureIntent.resolveActivity(packageManager)?.also {
            // 사진을 저장할 파일을 생성
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            // 파일이 성공적으로 생성되면 사진 URI를 설정하고 촬영 시작
            photoFile?.let {
                val photoURI: Uri = FileProvider.getUriForFile(this, "com.example.test_1.fileprovider", it)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)  // 사진 촬영 결과 처리
            }
        }
    }

    // 이미지 파일을 생성하는 메서드
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // 시간 기반 파일 이름 생성
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        // 저장할 디렉토리 설정
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        // 임시 파일 생성
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        // 생성된 파일 경로 저장
        currentPhotoPath = image.absolutePath
        return image
    }

    // 사진 촬영 후 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // 촬영한 이미지 파일을 읽어들여 이미지뷰에 표시
            val imgFile = File(currentPhotoPath)
            println("good")
            if (imgFile.exists()) {
                imageView.setImageURI(Uri.fromFile(imgFile))  // 이미지뷰에 이미지 표시

                // 이미지를 비트맵으로 변환 후 업로드
                bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                println("Image captured successfully. Displaying upload button.")
                // UI 업데이트는 메인 스레드에서 실행되어야 하므로 runOnUiThread 사용
                runOnUiThread {
                    uploadButton.visibility = View.VISIBLE
                }

            } else {
                println("Image file does not exist.")
            }
        } else {
            println("Request code or result code mismatch.")
        }

    }

    // 이미지를 서버로 업로드하는 메서드
    private fun uploadImage(bitmap: Bitmap) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // HTTP 클라이언트 초기화
                val client = OkHttpClient()
                // 비트맵 이미지를 JPEG 형식으로 압축하여 바이트 배열로 변환
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()

                // 요청 본문의 미디어 타입 설정
                val mediaType = "image/jpeg".toMediaType()
                // 바이트 배열을 요청 본문으로 변환
                val requestBody: RequestBody = byteArray.toRequestBody(mediaType)

                // 멀티파트 요청 본문 생성
                val multipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "image.jpg", requestBody)
                    .build()

                // POST 요청 생성
                val request = Request.Builder()
                    .url("http://172.30.1.44:5000")  // 로컬 Flask 서버 주소
                    .post(multipartBody)
                    .build()

                // 서버에 요청 실행 및 응답 처리
                val response = client.newCall(request).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // 업로드 성공 메시지 표시
                        Toast.makeText(this@MainActivity, "Upload successful!", Toast.LENGTH_SHORT).show()
                    } else {
                        // 업로드 실패 메시지 표시
                        Toast.makeText(this@MainActivity, "Upload failed: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // 예외 발생 시 로그 출력
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "An error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
