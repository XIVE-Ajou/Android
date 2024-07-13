package com.ajou.xive.network.api

import com.ajou.xive.network.model.NFCResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NFCService {
    @POST("decrypt")
    suspend fun postNFCTicket(
        @Body body : RequestBody
    ): Response<NFCResponse>
}