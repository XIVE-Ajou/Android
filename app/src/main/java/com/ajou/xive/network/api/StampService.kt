package com.ajou.xive.network.api

import com.ajou.xive.network.model.StampImgResponse
import com.ajou.xive.network.model.StampRegisteredResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface StampService {
    @GET("event/{eventId}/stamps/landing")
    suspend fun getInitStamp(
        @Header("AccessToken") accessToken : String,
        @Header("RefreshToken") refreshToken : String,
        @Path("eventId") eventId: String
    ): Response<ResponseBody>

    @GET("stamps/tagging")
    suspend fun getStampId(
        @Header("AccessToken") accessToken : String,
        @Header("RefreshToken") refreshToken : String,
        @Query("stampToken") stampToken : String
    ): Response<ResponseBody>

    @GET("event/{eventId}/stamps")
    suspend fun getEventStamps(
        @Header("AccessToken") accessToken : String,
        @Header("RefreshToken") refreshToken : String,
        @Path("eventId") eventId: String
    ): Response<ResponseBody>
}