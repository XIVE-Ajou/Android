package com.ajou.xive.network.model

import com.ajou.xive.home.model.Schedule
import com.google.gson.annotations.SerializedName

data class ScheduleResponse(
    @SerializedName("data") val data : List<Schedule>
)
