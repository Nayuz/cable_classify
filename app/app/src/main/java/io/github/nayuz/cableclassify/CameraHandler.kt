package io.github.nayuz.cableclassify

import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider

class CameraHandler(
    private val activity: AppCompatActivity,
    private val imageCaptureManager: ImageCaptureManager,
    private val imageView: ImageView,
    private val uploadButtonVisibilityCallback: (Boolean) -> Unit
) {

    private val takePictureLauncher: ActivityResultLauncher<Uri> =
        activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                val bitmap = imageCaptureManager.getBitmapFromFile()
                bitmap?.let {
                    imageView.setImageBitmap(it)
                    uploadButtonVisibilityCallback(true) // 업로드 버튼을 보이게 설정
                } ?: run {
                    Toast.makeText(activity, "Failed to capture image", Toast.LENGTH_SHORT).show()
                }
            }
        }

    fun launchCamera() {
        imageCaptureManager.createImageFile().also { file ->
            val photoUri = FileProvider.getUriForFile(
                activity,
                "io.github.nayuz.cableclassify.fileprovider",
                file
            )
            imageCaptureManager.currentPhotoUri = photoUri
            takePictureLauncher.launch(photoUri) // 촬영 시작
        }
    }
}