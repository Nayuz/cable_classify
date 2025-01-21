package io.github.nayuz.cableclassify

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ImageProcessor(private val context: Context) {

    private val imageTransferManager = ImageTransferManager("http://192.168.10.122:25565/")

    suspend fun processImage(bitmap: Bitmap, callback: (JSONObject?) -> Unit) {
        // Bitmap을 File로 변환
        val imageFile = convertBitmapToFile(bitmap)

        if (imageFile.exists()) {
            val responseJson = withContext(Dispatchers.IO) {
                try {
                    val responseStream = imageTransferManager.sendImageAsStream(imageFile)
                    responseStream?.let { parseJsonResponse(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            callback(responseJson)
        } else {
            println("Image file does not exist.")
            callback(null)
        }
    }

    // Bitmap을 File로 변환하는 함수
    private fun convertBitmapToFile(bitmap: Bitmap): File {
        val file = File(context.cacheDir, "uploaded_image.jpg")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
        }
        return file
    }

    private fun prepareImageFile(): File {
        val inputStream = context.resources.openRawResource(R.raw.cabletest)
        val file = File(context.cacheDir, "cabletest.jpg")
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        return file
    }

    // 응답 스트림을 JSON으로 파싱
    private fun parseJsonResponse(responseStream: InputStream): JSONObject? {
        try {
            val jsonResponse = responseStream.bufferedReader().use { it.readText() }
            return JSONObject(jsonResponse)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
