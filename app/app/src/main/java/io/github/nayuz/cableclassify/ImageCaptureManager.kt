package io.github.nayuz.cableclassify


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImageCaptureManager(private val context: Context) {
    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }

    var currentPhotoPath: String = ""
    var currentPhotoUri: Uri? = null // URI를 추가

    // 카메라 촬영을 위한 Intent를 실행하는 메서드
    fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.resolveActivity(context.packageManager)?.also {
            val photoFile: File? = try {
                createImageFile() // 이미지 파일 생성
            } catch (ex: IOException) {
                null
            }
            photoFile?.let {
                // currentPhotoUri 업데이트
                currentPhotoUri = FileProvider.getUriForFile(
                    context,
                    "io.github.nayuz.cableclassify.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                (context as AppCompatActivity).startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE) // 카메라 실행
            }
        }
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir: File? = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        currentPhotoPath = image.absolutePath
        return image
    }

    // getBitmapFromFile은 기존대로 유지
    fun getBitmapFromFile(): Bitmap? {
        val imgFile = File(currentPhotoPath)
        return if (imgFile.exists()) {
            BitmapFactory.decodeFile(imgFile.absolutePath)
        } else {
            null
        }
    }
}