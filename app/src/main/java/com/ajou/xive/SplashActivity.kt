package com.ajou.xive

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.ajou.xive.auth.SignUpActivity
import com.ajou.xive.databinding.ActivitySplashBinding
import com.ajou.xive.onboarding.view.OnBoardingActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private var _binding: ActivitySplashBinding? = null
    private val binding get() = _binding!!
    private var accessToken : String ?= null
    private var refreshToken : String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gifDrawable = pl.droidsonroids.gif.GifDrawable(resources, R.drawable.splash_logo)
        binding.logo.setImageDrawable(gifDrawable)
        gifDrawable.setSpeed(0.8f)

        Handler().postDelayed({
            CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
                if (accessToken != null && refreshToken != null){
                    val intent = Intent(this@SplashActivity, OnBoardingActivity::class.java)
                    startActivity(intent)
                }else{
                    val intent = Intent(this@SplashActivity, SignUpActivity::class.java)
                    startActivity(intent)
                }
                // TODO 서버와 연결 후에는 token 여부와 firstFlag로 식별하기
            }
        },2000)
    }

    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.d("exceptionHandler", exception.message.toString())
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