package com.ajou.xive

import android.app.Application
import android.content.Context
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility

class App : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance : App? = null

        fun context() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.NATIVE_API_KEY)
    }
}