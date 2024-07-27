package com.ajou.xive.home.model

import  kotlinx.serialization.Serializable

@Serializable
data class TicketVisitedFlag(
    val ticketId : Int,
    val isVisited : Boolean // nfc 태그 후 최초 접근 여부 판별 플래그
)
