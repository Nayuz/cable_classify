package com.example.test_1

import android.graphics.Bitmap
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class ImageUploader(private val context: MainActivity) {

    // 이미지를 서버로 업로드하는 함수
    fun uploadImage(bitmap: Bitmap) {
        // 백그라운드에서 실행할 코드를 코루틴을 사용하여 IO 디스패처에서 실행
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()  // OkHttp 클라이언트 초기화
                val byteArrayOutputStream = ByteArrayOutputStream()  // 바이트 배열로 이미지를 변환하기 위한 스트림
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)  // 이미지를 JPEG 형식으로 압축
                val byteArray = byteArrayOutputStream.toByteArray()  // 이미지 바이트 배열로 변환

                val mediaType = "image/jpeg".toMediaType()  // 미디어 타입 설정 (JPEG 이미지)
                val requestBody: RequestBody = byteArray.toRequestBody(mediaType)  // 요청 바디 생성

                // 멀티파트 요청 바디 생성 (파일 업로드 형식)
                val multipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "image.jpg", requestBody)  // 파일 이름과 요청 바디 추가
                    .build()

                // HTTP POST 요청 생성
                val request = Request.Builder()
                    .url("http://192.168.10.77:5000")  // 업로드할 서버 URL
                    .post(multipartBody)  // POST 요청에 멀티파트 바디 추가
                    .build()

                // 서버로 요청을 보내고 응답 받기
                val response = client.newCall(request).execute()

                // 메인 스레드에서 UI 작업 (Toast 메시지 표시)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {  // 서버 응답이 성공적이면
                        Toast.makeText(context, "Upload successful!", Toast.LENGTH_SHORT).show()  // 성공 메시지 표시
                    } else {
                        Toast.makeText(context, "Upload failed: ${response.message}", Toast.LENGTH_SHORT).show()  // 실패 메시지 표시
                    }
                }
            } catch (e: Exception) {  // 예외 처리
                e.printStackTrace()  // 예외 발생 시 스택 트레이스 출력
                withContext(Dispatchers.Main) {  // 메인 스레드에서 UI 작업
                    Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()  // 에러 메시지 표시
                }
            }
        }
    }
}

