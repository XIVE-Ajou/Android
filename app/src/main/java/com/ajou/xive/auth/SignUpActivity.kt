package com.ajou.xive.auth

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.ajou.xive.UserDataStore
import com.ajou.xive.databinding.ActivitySignUpBinding
import com.ajou.xive.home.view.HomeActivity
import com.ajou.xive.network.RetrofitInstance
import com.ajou.xive.network.api.UserService
import com.ajou.xive.onboarding.view.OnBoardingActivity
import com.ajou.xive.setOnSingleClickListener
import com.google.gson.JsonObject
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject

class SignUpActivity : AppCompatActivity() {
    private var _binding : ActivitySignUpBinding ?= null
    private val binding get() = _binding!!
    private val userService = RetrofitInstance.getInstance().create(UserService::class.java)
    private val dataStore = UserDataStore()
    private val viewModel : KakaoSignUpViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.loginBtn.setOnClickListener {
//            val intent = Intent(this,KakaoSignUpActivity::class.java)
//            startActivity(intent)
            kakaoLogin()
        }

        viewModel.kakaoToken.observe(this, Observer {
            if (viewModel.kakaoToken.value!!.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
                    val jsonObject = JsonObject().apply {
                        addProperty("accessToken", viewModel.kakaoToken.value.toString())
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
                        val getMemberInfoDeferred = async { userService.getMemberInfo(tokenBody.accessToken, tokenBody.refreshToken) }
                        val getMemberInfoResponse = getMemberInfoDeferred.await()
                        if (getMemberInfoResponse.isSuccessful) {
                            val memberInfoBody = JSONObject(getMemberInfoResponse.body()?.string())
                            dataStore.saveLoginType(memberInfoBody.getString("loginType"))
                            dataStore.saveNickname(memberInfoBody.getString("nickname"))
                        }
                        withContext(Dispatchers.Main){
                            if (tokenBody.isNew){
                                val intent = Intent(this@SignUpActivity, OnBoardingActivity::class.java)
                                startActivity(intent)
                            } else {
                                val intent = Intent(this@SignUpActivity, HomeActivity::class.java)
                                startActivity(intent)
                            }

                        }
                    }else{
                        Log.d("kakao login fail",loginResponse.errorBody()?.string().toString())
                    }
                }

            }
        })

//        binding.nonMemBtn.setOnSingleClickListener {
//            CoroutineScope(Dispatchers.IO).launch(exceptionHandler){
//                val loginDeferred = async { userService.nonLogin() }
//                val loginResponse = loginDeferred.await()
//                if (loginResponse.isSuccessful){
//                    val tokenBody = loginResponse.body()
//                    dataStore.saveAccessToken(tokenBody!!.accessToken)
//                    dataStore.saveRefreshToken(tokenBody.refreshToken)
//                    val getMemberInfoDeferred = async { userService.getMemberInfo(tokenBody.accessToken, tokenBody.refreshToken) }
//                    val getMemberInfoResponse = getMemberInfoDeferred.await()
//                    if (getMemberInfoResponse.isSuccessful) {
//                        val memberInfoBody = JSONObject(getMemberInfoResponse.body()?.string())
//                        dataStore.saveLoginType(memberInfoBody.getString("loginType"))
//                        dataStore.saveNickname(memberInfoBody.getString("nickname"))
//                    }
//                    withContext(Dispatchers.Main){
//                        val intent = Intent(this@SignUpActivity, OnBoardingActivity::class.java)
//                        startActivity(intent)
//                    }
//                }else{
//                    Log.d("nonmember login fail",loginResponse.errorBody()?.string().toString())
//                }
//            }
//        }
    }

    private fun kakaoLogin(){
        // 카카오톡 설치 확인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                // 로그인 실패 부분
                if (error != null) {
                    Log.e(ContentValues.TAG, "로그인 실패 $error")
                    // 사용자가 취소
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                        UserApiClient.instance.loginWithKakaoAccount(
                            this,
                            callback = mCallback
                        ) // 카카오 이메일 로그인
                }
                // 로그인 성공 부분
                else if (token != null) {
                    UserApiClient.instance.me { user, error ->
                        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
                            dataStore.saveNickname(user?.kakaoAccount?.profile?.nickname.toString())
                            viewModel.setKakaoToken(token.accessToken)
//                            dataStore.saveKakaoId(user?.id.toString())
                        }
                    }
                }
            }
        } else {
            Log.d(ContentValues.TAG,"카카오계정으로 로그인 시도")
            UserApiClient.instance.loginWithKakaoAccount(
                this,
                callback = mCallback
            ) // 카카오 이메일 로그인
        }
    }

    private val mCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(ContentValues.TAG, "카카오계정으로 로그인 실패", error)
        } else if (token != null) {
            Log.d(ContentValues.TAG,"카카오계정으로 로그인 성공 $token")
            CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
                viewModel.setKakaoToken(token.accessToken)
            }
        }else{
            Log.d(ContentValues.TAG,"에러 $error  token $token")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }
    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
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
