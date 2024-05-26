package com.ajou.xive.network.api

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface InquireService {
    @POST("inquiries")
    suspend fun postInquire(
        @Header("AccessToken") accessToken : String,
        @Header("RefreshToken") refreshToken : String,
        @Body inquire : RequestBody
    ) :Response<RequestBody>
}