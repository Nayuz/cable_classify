package io.github.nayuz.cableclassify

import android.graphics.Bitmap
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AlbumHelper(private val context: MainActivity) {

    // 앨범 디렉토리를 정의하는 lazy 프로퍼티
    private val albumDirectory: File by lazy {
        // MyAlbum 폴더 경로를 설정
        val albumDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyAlbum")

        // 폴더가 존재하지 않으면 새로 생성
        if (!albumDir.exists()) {
            albumDir.mkdirs()
        }
        albumDir
    }

    // 이미지를 앨범에 저장하는 함수
    fun saveImageToAlbum(bitmap: Bitmap) {
        // 비트맵이 null이 아닌 경우에만 저장 진행
        if (bitmap != null) {
            // 현재 시간으로 파일 이름 생성 (예: IMG_20250121_150101.jpg)
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "IMG_${timeStamp}.jpg"

            // 앨범 디렉토리에 파일 생성
            val file = File(albumDirectory, fileName)

            try {
                // 파일을 쓰기 위한 FileOutputStream 생성
                val fos = FileOutputStream(file)

                // 비트맵을 JPEG 형식으로 압축하여 파일에 저장
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)

                // 파일 출력 스트림을 플러시하고 닫음
                fos.flush()
                fos.close()

                // 이미지가 저장되었다는 메시지 표시
                Toast.makeText(context, "Image saved to album!", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                // 예외 발생 시 스택 트레이스를 출력하고 오류 메시지 표시
                e.printStackTrace()
                Toast.makeText(context, "Failed to save image to album", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
