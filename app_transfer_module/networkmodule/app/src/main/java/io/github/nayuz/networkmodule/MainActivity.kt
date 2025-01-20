package io.github.nayuz.networkmodule

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        enableEdgeToEdge()

        val imageProcessor = ImageProcessor(this)

        lifecycleScope.launch {
            imageProcessor.processImage { bitmap ->
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                } else {
                    println("Failed to process the image.")
                }
            }
        }
    }
}
