package com.ajou.xive

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ajou.xive.databinding.ActivityNetworkErrorBinding
import com.ajou.xive.home.view.HomeActivity

class NetworkErrorActivity : AppCompatActivity() {
    private lateinit var binding : ActivityNetworkErrorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNetworkErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.retry.setOnClickListener {
            finish()
        }
        binding.home.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}