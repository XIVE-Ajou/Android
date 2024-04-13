package com.ajou.xive.network.api

import com.ajou.xive.network.model.TokenResponse
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserService {
    @POST("kakao-login")
    suspend fun login(@Body authCode: RequestBody)
    : Response<TokenResponse>

    @POST("non-login")
    suspend fun nonLogin() : Response<TokenResponse>
}