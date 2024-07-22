package com.ajou.xive.network.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface EventService {
    @GET("event/tagging")
    suspend fun getEvent(
        @Header("AccessToken") accessToken : String,
        @Header("RefreshToken") refreshToken : String,
        @Query("eventToken") eventToken : String
    ): Response<ResponseBody>
}