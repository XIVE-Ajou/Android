package com.ajou.xive.auth

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.ajou.xive.BuildConfig
import com.ajou.xive.R
import com.ajou.xive.UserDataStore
import com.ajou.xive.network.RetrofitInstance
import com.ajou.xive.network.api.UserService
import com.ajou.xive.onboarding.view.OnBoardingActivity
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class KakaoSignUpActivity : AppCompatActivity() {
    val clientId = BuildConfig.CLIENT_ID // apikey.properties에 있음
    val redirectUri = BuildConfig.REDIRECT_URI
    val responseType = "code"
    private lateinit var viewModel : KakaoSignUpViewModel
    private val userService = RetrofitInstance.getInstance().create(UserService::class.java)
    private val dataStore = UserDataStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kakao_sign_up)
        viewModel = ViewModelProvider(this)[KakaoSignUpViewModel::class.java]

        val webview : WebView = findViewById(R.id.webview)

        webview.settings.apply {
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        webview.clearCache(true)
        webview.clearHistory()
        webview.clearFormData()

        webview.loadUrl("https://kauth.kakao.com/oauth/authorize?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=${responseType}")

        webview.webViewClient = object : WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                val target = redirectUri.plus("?code=")// code 앞에 들어갈 것은 redirectURI
                if (url!!.substring(target.indices) == target) {
                    val code = url!!.substring(target.length)
                    viewModel.setAuthCode(code)
                }
            }
        }

        viewModel.authCode.observe(this) {
            if (viewModel.authCode.value!!.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
                    val jsonObject = JsonObject().apply {
                        addProperty("authCode", viewModel.authCode.value.toString())
                    }
                    val requestBody = RequestBody.create(
                        "application/json".toMediaTypeOrNull(),
                        jsonObject.toString()
                    )
                    val loginDeferred = async { userService.login(requestBody) }
                    val loginResponse = loginDeferred.await()
                    if (loginResponse.isSuccessful){
                        val tokenBody = loginResponse.body()
                        dataStore.saveAccessToken(tokenBody!!.accessToken)
                        dataStore.saveRefreshToken(tokenBody.refreshToken)
                        Log.d("kakao login success",tokenBody.toString())
                        val intent = Intent(this@KakaoSignUpActivity, OnBoardingActivity::class.java)
                        startActivity(intent)
                    }else{
                        Log.d("kakao login fail",loginResponse.errorBody()?.string().toString())
                    }
                }

            }
        }
    }

    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.d("exceptionHandler",exception.message.toString())
        showErrorDialog()
    }
    private fun showErrorDialog() {
        runOnUiThread {
            AlertDialog.Builder(this).apply {
                setTitle("Error")
                setMessage("Network request failed.")
                setPositiveButton("종료") { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                setCancelable(false)
                show()
            }
        }
    }
}