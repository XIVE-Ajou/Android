package com.ajou.xive.network.model

data class TokenResponse(
    val accessToken : String,
    val refreshToken : String,
    val isNew : Boolean
)
