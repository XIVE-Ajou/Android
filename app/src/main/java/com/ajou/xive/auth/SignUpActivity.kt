package com.ajou.xive.auth

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.ajou.xive.R
import com.ajou.xive.UserDataStore
import com.ajou.xive.databinding.ActivitySignUpBinding
import com.ajou.xive.network.RetrofitInstance
import com.ajou.xive.network.api.UserService
import com.ajou.xive.onboarding.view.OnBoardingActivity
import kotlinx.coroutines.*

class SignUpActivity : AppCompatActivity() {
    private var _binding : ActivitySignUpBinding ?= null
    private val binding get() = _binding!!
    private val userService = RetrofitInstance.getInstance().create(UserService::class.java)
    private val dataStore = UserDataStore()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener {
            val intent = Intent(this,KakaoSignUpActivity::class.java)
            startActivity(intent)
        }

        binding.nonMemBtn.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.nonMemBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch(exceptionHandler){
                val loginDeferred = async { userService.nonLogin() }
                val loginResponse = loginDeferred.await()
                if (loginResponse.isSuccessful){
                    val tokenBody = loginResponse.body()
                    Log.d("nonmember token",tokenBody.toString())
                    dataStore.saveAccessToken(tokenBody!!.accessToken)
                    dataStore.saveRefreshToken(tokenBody!!.refreshToken)
                    Log.d("nonmember login success",tokenBody.toString())
                    val intent = Intent(this@SignUpActivity, OnBoardingActivity::class.java)
                    startActivity(intent)
                }else{
                    Log.d("nonmember login fail",loginResponse.errorBody()?.string().toString())
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
