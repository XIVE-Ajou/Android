package com.ajou.xive.network.model

import com.ajou.xive.home.model.StampImg
import com.ajou.xive.home.model.StampRegistered
import com.google.gson.annotations.SerializedName

data class StampRegisteredResponse(
    @SerializedName("data") val data : List<StampRegistered>
)
