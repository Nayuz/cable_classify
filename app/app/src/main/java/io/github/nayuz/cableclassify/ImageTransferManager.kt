package io.github.nayuz.cableclassify


import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.InputStream


// 응답 데이터 클래스 정의
data class ApiResponse(
    val cableType: String,
    val confidence: Double
)

class ImageTransferManager(baseUrl: String) {

    private val apiService: ImageApiService

    init {
        // HTTP 로깅 인터셉터 설정
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // OkHttpClient 설정
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        // Retrofit 설정
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        apiService = retrofit.create(ImageApiService::class.java)
    }

    // 이미지 업로드 메서드
    suspend fun sendImageAsStream(file: File): InputStream? {
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestBody)

        return try {
            val response = apiService.uploadImage(multipartBody) // Response<ResponseBody> 타입
            if (response.isSuccessful) { // HTTP 상태 코드 확인
                response.body()?.byteStream()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
