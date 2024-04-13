package com.ajou.xive.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ajou.xive.R
import com.ajou.xive.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private var _binding : ActivityHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}