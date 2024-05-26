package com.ajou.xive.network.model

data class NFCResponse(
    val status : String,
    val eventId: String,
    val nfcId : String,
    val seatNumber: String
)
