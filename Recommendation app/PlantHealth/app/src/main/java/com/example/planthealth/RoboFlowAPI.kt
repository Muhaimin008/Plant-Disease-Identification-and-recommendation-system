package com.example.planthealth

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface RoboFlowAPI {
    @Multipart
    @POST("{model_id}")
    fun uploadImage(
        @Query("api_key") apiKey: String,
        @Part image: MultipartBody.Part
    ): Call<ResponseBody>
}

