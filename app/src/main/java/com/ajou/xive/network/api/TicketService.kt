package com.ajou.xive.network.api

import com.ajou.xive.home.model.Ticket
import com.ajou.xive.home.model.NFCData
import com.ajou.xive.network.model.TicketResponse
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface TicketService {
    @GET("tickets")
    suspend fun getAllTickets(
        @Header("AccessToken") accessToken : String,
        @Header("RefreshToken") refreshToken : String
    ): Response<TicketResponse>

    @POST("tickets")
    suspend fun postTicket(
        @Header("AccessToken") accessToken : String,
        @Header("RefreshToken") refreshToken : String,
        @Body ticket : NFCData
    ) : Response<Ticket>

    @GET("tickets/{ticketId}")
    suspend fun getOneTicket(
        @Header("AccessToken") accessToken : String,
        @Header("RefreshToken") refreshToken : String,
        @Path("ticketId") ticketId : String
    ): Response<Ticket>

    @DELETE("tickets/{ticketId}")
    suspend fun deleteTicket(
        @Header("AccessToken") accessToken : String,
        @Header("RefreshToken") refreshToken : String,
        @Path("ticketId") ticketId : String
    ): Response<ResponseBody>

    @POST("exhibition-tickets")
    suspend fun postExhibitTicket(
        @Header("AccessToken") accessToken : String,
        @Header("RefreshToken") refreshToken : String,
        @Body eventId: RequestBody
    ): Response<Ticket>
    @Multipart
    @POST("tickets/image/{ticketId}")
    suspend fun postTicketImage(
        @Header("AccessToken") accessToken : String,
        @Header("RefreshToken") refreshToken : String,
        @Path("ticketId") ticketId : String,
        @Part file : MultipartBody.Part
    ): Response<ResponseBody>
}