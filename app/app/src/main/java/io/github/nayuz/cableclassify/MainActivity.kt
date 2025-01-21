package io.github.nayuz.cableclassify

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var uploadButton: Button  // 업로드 버튼 변수 선언
    private var bitmap: Bitmap? = null // 비트맵을 전역 변수로 저장

    // 모듈화된 객체들
    private lateinit var permissionManager: PermissionManager
    private lateinit var imageCaptureManager: ImageCaptureManager
    private lateinit var imageProcessor : ImageProcessor
    private lateinit var cameraHandler: CameraHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 레이아웃에서 이미지뷰와 버튼 초기화
        imageView = findViewById(R.id.imageView)
        val takePictureButton: Button = findViewById(R.id.takePictureButton)
        uploadButton = findViewById(R.id.uploadButton)

        // 모듈화된 객체 초기화
        permissionManager = PermissionManager(this)
        imageCaptureManager = ImageCaptureManager(this)
        imageProcessor = ImageProcessor(this)

        // 초기 상태에서 업로드 버튼 숨기기
        uploadButton.visibility = View.GONE

        // CameraHandler 초기화
        cameraHandler = CameraHandler(
            activity = this,
            imageCaptureManager = imageCaptureManager,
            imageView = imageView,
            uploadButtonVisibilityCallback = { isVisible ->
                uploadButton.visibility = if (isVisible) View.VISIBLE else View.GONE
            }
        )

        // 카메라 권한 확인
        permissionManager.checkCameraPermission()

        // 카메라 권한이 승인되었을 때 사진 촬영 시작
        takePictureButton.setOnClickListener {
            cameraHandler.launchCamera()
        }

        // 업로드 버튼 클릭 시 이미지 업로드
        uploadButton.setOnClickListener {
            bitmap?.let {
                // 업로드 진행
                CoroutineScope(Dispatchers.Main).launch {
                    // 이미지를 처리하고 결과를 받아서 업로드 진행
                    imageProcessor.processImage(it) { jsonResponse ->
                        jsonResponse?.let {
                            // JSON 결과를 처리하고 이미지 뷰에 결과를 표시하는 로직
                            Toast.makeText(
                                this@MainActivity,
                                "Image processed and uploaded successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        } ?: run {
                            Toast.makeText(
                                this@MainActivity,
                                "Image processing failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } ?: run {
                Toast.makeText(this, "No image to upload", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.handlePermissionResult(
            requestCode,
            grantResults,
            onPermissionGranted = {
                // 권한이 승인되었으면 사진 촬영 시작
                imageCaptureManager.dispatchTakePictureIntent()
            },
            onPermissionDenied = {
                // 권한이 거부되었으면 메시지 표시
                showPermissionDeniedMessage()
            }
        )
    }

    // 권한 거부 시 사용자에게 알림 메시지 표시
    private fun showPermissionDeniedMessage() {
        Toast.makeText(this, "Camera permission is required to take a picture", Toast.LENGTH_LONG).show()
    }

    // 사진 촬영 후 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ImageCaptureManager.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            bitmap = imageCaptureManager.getBitmapFromFile()
            bitmap?.let {
                imageView.setImageBitmap(it)
                uploadButton.visibility = View.VISIBLE
            }
        }
    }
}