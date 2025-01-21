package io.github.nayuz.cableclassify

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraHelper(private val context: Context) {

    private val CAMERA_PERMISSION_REQUEST_CODE = 100  // 카메라 권한 요청 코드
    private val REQUEST_IMAGE_CAPTURE = 1  // 이미지 촬영 요청 코드
    private var currentPhotoPath: String = ""  // 현재 사진 파일 경로 저장

    // 카메라 권한을 체크하고, 권한이 없으면 요청하는 함수
    fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 요청
            ActivityCompat.requestPermissions(context as MainActivity, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    // 카메라 앱을 호출하여 사진을 촬영하는 함수
    fun dispatchTakePictureIntent(requestCode: Int) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)  // 사진 촬영 액션 Intent 생성
        takePictureIntent.resolveActivity(context.packageManager)?.also {
            // 카메라 앱을 열 수 있으면 사진 파일 생성
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null  // 파일 생성 중 오류가 나면 null 반환
            }
            photoFile?.let {
                // 파일 URI를 얻기 위해 FileProvider 사용
                val photoURI: Uri = FileProvider.getUriForFile(context, "io.github.nayuz.cableclassify.fileprovider", it)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)  // 사진 저장 위치를 지정
                // 촬영 후 결과를 받을 수 있도록 ActivityForResult 호출
                (context as MainActivity).startActivityForResult(takePictureIntent, requestCode)
            }
        }
    }

    // 사진을 저장할 파일을 생성하는 함수
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())  // 타임스탬프를 파일명에 추가
        val imageFileName = "JPEG_${timeStamp}_"  // 파일명 생성
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)  // 저장할 디렉토리 지정
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)  // 임시 파일 생성
        currentPhotoPath = image.absolutePath  // 파일 경로 저장
        return image
    }

    // 현재 사진 파일 경로를 반환하는 함수
    fun getCurrentPhotoPath(): String {
        return currentPhotoPath
    }
}

