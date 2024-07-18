package com.ajou.xive.network.model

import com.ajou.xive.home.model.StampImg
import com.google.gson.annotations.SerializedName

data class StampImgResponse(
    @SerializedName("data") val data : List<StampImg>
)
