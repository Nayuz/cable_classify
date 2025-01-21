package io.github.nayuz.cableclassify

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImageApiService {
    @Multipart
    @POST("/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>
}
