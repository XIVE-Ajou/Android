package com.ajou.xive.network.api

import com.ajou.xive.network.model.ScheduleResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SchedulesService {
    @GET("schedules")
    suspend fun getMonthSchedules(
        @Header("AccessToken") accessToken : String,
        @Header("RefreshToken") refreshToken : String,
        @Query("yearMonth") yearMonth : String
    ): Response<ScheduleResponse>
}