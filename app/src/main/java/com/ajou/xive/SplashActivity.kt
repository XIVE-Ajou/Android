package com.ajou.xive

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.ajou.xive.auth.SignUpActivity
import com.ajou.xive.databinding.ActivitySplashBinding
import com.ajou.xive.home.HomeActivity
import com.ajou.xive.onboarding.view.OnBoardingActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.*
import java.net.ConnectException

class SplashActivity : AppCompatActivity() {
    private var _binding: ActivitySplashBinding? = null
    private val binding get() = _binding!!
    private var accessToken : String ?= null
    private var refreshToken : String ?= null
    private val dataStore = UserDataStore()

    override fun onCreate(savedInstanceState: Bundle?) {
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

                    withContext(Dispatchers.Main){
                        if (accessToken != null && refreshToken != null){
                            val intent = Intent(this@SplashActivity, HomeActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }else{
                            val intent = Intent(this@SplashActivity, SignUpActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
            }
        },2000)
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

    override fun onStop() {
        super.onStop()
        finish()
    }
    override fun onDestroy() {
        super.onDestroy()
        finish()
    }
}