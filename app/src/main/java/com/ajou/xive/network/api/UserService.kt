package com.ajou.xive.network.api

import com.ajou.xive.network.model.TokenResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface UserService {
    @POST("kakao-login")
    suspend fun login(@Body authCode: RequestBody)
    : Response<TokenResponse>

    @POST("non-login")
    suspend fun nonLogin() : Response<TokenResponse>

    @GET("members")
    suspend fun getMemberInfo(
        @Header("AccessToken") accessToken : String,
        @Header("RefreshToken") refreshToken : String
    ) : Response<ResponseBody>

    @POST("withdrawal")
    suspend fun withdrawal(
        @Header("AccessToken") accessToken : String?,
        @Header("RefreshToken") refreshToken : String?,
        @Body reason : RequestBody
    ) : Response<ResponseBody>
}