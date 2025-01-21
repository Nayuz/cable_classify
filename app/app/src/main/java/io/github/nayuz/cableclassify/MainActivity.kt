package io.github.nayuz.cableclassify

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var uploadButton: Button  // 업로드 버튼 변수 선언
    private lateinit var saveToAlbumButton: Button  // 앨범에 저장 버튼
    private lateinit var openAlbumButton: Button  // 앨범 열기 버튼


    // 모듈화된 객체들
    private lateinit var albumHelper: AlbumHelper
    private lateinit var cameraHelper: CameraHelper
    private lateinit var imageUploader: ImageUploader
    private var bitmap: Bitmap? = null // 비트맵을 전역 변수로 저장

    private val REQUEST_IMAGE_CAPTURE = 1  // 카메라에서 촬영한 이미지를 받을 요청 코드
    private val REQUEST_IMAGE_PICK = 2  // 갤러리에서 이미지를 선택할 요청 코드

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // 레이아웃 설정

        // UI 요소들 연결
        imageView = findViewById(R.id.imageView)
        val takePictureButton: Button = findViewById(R.id.takePictureButton)
        uploadButton = findViewById(R.id.uploadButton)
        saveToAlbumButton = findViewById(R.id.saveToAlbumButton)
        openAlbumButton = findViewById(R.id.openAlbumButton)

        // 기본적으로 업로드와 저장 버튼은 숨기고, 앨범 열기 버튼만 보이도록 설정
        uploadButton.visibility = View.GONE
        saveToAlbumButton.visibility = View.GONE
        openAlbumButton.visibility = View.VISIBLE

        //객체 초기화
        cameraHelper = CameraHelper(this)
        albumHelper = AlbumHelper(this)
        imageUploader = ImageUploader(this)


        // 카메라 권한 확인
        cameraHelper.checkCameraPermission()

        // 촬영 버튼 클릭 시 카메라를 실행하여 사진 찍기
        takePictureButton.setOnClickListener {
            cameraHelper.dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE)
        }

        // 업로드 버튼 클릭 시 이미지를 업로드
        uploadButton.setOnClickListener {
            bitmap?.let { imageUploader.uploadImage(it) }  // bitmap이 null이 아니면 업로드 실행
        }

        // 앨범에 이미지 저장 버튼 클릭 시 앨범에 이미지 저장
        saveToAlbumButton.setOnClickListener {
            bitmap?.let { albumHelper.saveImageToAlbum(it) }  // bitmap이 null이 아니면 앨범에 저장
        }

        // 앨범 버튼 클릭 시 앨범 액티비티 열기
        openAlbumButton.setOnClickListener {
            val intent = Intent(this, AlbumActivity::class.java)  // 앨범 액티비티로 이동하는 인텐트
            startActivityForResult(intent, REQUEST_IMAGE_PICK)  // 앨범 액티비티를 실행
        }
    }

    // 사진 촬영 후 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 카메라 촬영 후 이미지를 받았을 때
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imgFile = File(cameraHelper.getCurrentPhotoPath())  // 카메라로 찍은 사진 경로 가져오기
            if (imgFile.exists()) {  // 이미지 파일이 존재하면
                imageView.setImageURI(Uri.fromFile(imgFile))  // 이미지 뷰에 이미지 표시
                bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)  // 비트맵으로 이미지 변환

                // 업로드와 저장 버튼을 보이도록 설정
                uploadButton.visibility = View.VISIBLE
                saveToAlbumButton.visibility = View.VISIBLE
            }
        }

        // 갤러리에서 이미지를 선택했을 때
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri = data.data!!  // 선택한 이미지 URI 가져오기
            val selectedBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImageUri))  // URI로부터 비트맵 생성
            imageView.setImageBitmap(selectedBitmap)  // 이미지 뷰에 비트맵 이미지 표시
            bitmap = selectedBitmap  // 비트맵 저장

            // 업로드와 저장 버튼을 보이도록 설정
            uploadButton.visibility = View.VISIBLE
            saveToAlbumButton.visibility = View.VISIBLE
        }
    }
}