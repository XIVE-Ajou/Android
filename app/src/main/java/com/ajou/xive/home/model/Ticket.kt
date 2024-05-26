package com.ajou.xive.home.model

data class Ticket(
    val ticketId: Int,
    val eventName: String,
    val eventRound: String,
    val startDate: String,
    val endDate: String,
    val eventDay: String,
    val startTime: String,
    val endTime: String,
    val eventImageUrl: String,
    val eventType: String,
    val eventPlace: String,
    val seatNumber: String,
    val eventWebUrl: String
)
