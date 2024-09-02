package com.ajou.xive

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ajou.xive.auth.SignUpActivity
import com.ajou.xive.databinding.ActivitySplashBinding
import com.ajou.xive.home.view.HomeActivity
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {
    private var _binding: ActivitySplashBinding? = null
    private val binding get() = _binding!!
    private var accessToken : String ?= null
    private var refreshToken : String ?= null
    private val dataStore = UserDataStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)

        _binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gifDrawable = pl.droidsonroids.gif.GifDrawable(resources, R.drawable.splash_logo)
        binding.logo.setImageDrawable(gifDrawable)
        gifDrawable.setSpeed(0.8f)

        Handler().postDelayed({
            CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
                accessToken = dataStore.getAccessToken()
                refreshToken = dataStore.getRefreshToken()
                withContext(Dispatchers.Main) {
                    if (accessToken != null && refreshToken != null) {
                        val intent = Intent(this@SplashActivity, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@SplashActivity, SignUpActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }
            }
        }, 2000)

    }

    override fun onStop() {
        super.onStop()
        finish()
    }
    override fun onDestroy() {
        super.onDestroy()
        finish()
    }
}