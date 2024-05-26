package com.ajou.xive.network.model

import com.ajou.xive.home.model.Ticket
import com.google.gson.annotations.SerializedName

data class TicketResponse(
    @SerializedName("data") val data : List<Ticket>
)
