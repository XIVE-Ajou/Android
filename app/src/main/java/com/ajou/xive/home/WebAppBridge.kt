package com.ajou.xive.home

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface

class WebAppBridge(private val mContext: Context) {
    @JavascriptInterface
    fun androidFunc(value:String): String {
        Log.d("value",value)
        return value
    }
}