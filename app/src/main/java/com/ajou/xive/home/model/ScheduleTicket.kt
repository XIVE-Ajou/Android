package com.ajou.xive.home.model

data class ScheduleTicket(
    val isNew: Boolean,
    val ticketId: Int,
    val eventId: Int,
    val eventName: String,
    val eventRound: String,
    val startDate: String,
    val endDate: String,
    val eventDay: String,
    val startTime: String,
    val endTime: String,
    val eventImageUrl: String,
    val eventBackgroundImageUrl: String,
    val eventType: String,
    val eventPlace: String,
    val seatNumber: String,
    val eventWebUrl: String
)
