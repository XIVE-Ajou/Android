package com.ajou.xive.auth

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ajou.xive.R
import com.ajou.xive.databinding.ActivitySignUpBinding
import com.ajou.xive.onboarding.view.OnBoardingActivity

class SignUpActivity : AppCompatActivity() {
    private var _binding : ActivitySignUpBinding ?= null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nonMemBtn.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.nonMemBtn.setOnClickListener {
            val intent = Intent(this, OnBoardingActivity::class.java)
            startActivity(intent)
        }

    }
}