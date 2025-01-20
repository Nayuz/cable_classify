package io.github.nayuz.networkmodule

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ImageProcessor(private val context: Context) {

    private val imageTransferManager = ImageTransferManager("http://192.168.10.122:25565/")

    suspend fun processImage(callback: (Bitmap?) -> Unit) {
        val testImage = prepareImageFile()

        if (testImage.exists()) {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val responseStream = imageTransferManager.sendImageAsStream(testImage)
                    responseStream?.let { BitmapFactory.decodeStream(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            callback(bitmap)
        } else {
            println("Test image file does not exist.")
            callback(null)
        }
    }

    private fun prepareImageFile(): File {
        val inputStream = context.resources.openRawResource(R.raw.cabletest)
        val file = File(context.cacheDir, "cabletest.jpg")
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        return file
    }
}
