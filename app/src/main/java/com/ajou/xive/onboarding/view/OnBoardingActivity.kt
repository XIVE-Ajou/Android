package com.ajou.xive.onboarding.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.ajou.xive.R
import com.ajou.xive.databinding.ActivityOnBoardingBinding
import com.ajou.xive.home.HomeActivity
import com.ajou.xive.onboarding.OnBoardingViewPagerAdapter

class OnBoardingActivity : AppCompatActivity() {
    private var _binding : ActivityOnBoardingBinding ?= null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pager.adapter = OnBoardingViewPagerAdapter(this)
        binding.dotsIndicator.attachTo(binding.pager)

        binding.skip.setOnClickListener {
            binding.pager.currentItem = 2
        }

        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if(position == 2) {
                    binding.skip.visibility = View.INVISIBLE
                    binding.nextBtn.setBackgroundResource(R.drawable.onboarding_confirm_btn)
                    binding.nextBtn.text = "시작하기"
                }
                else {
                    binding.skip.visibility = View.VISIBLE
                    binding.nextBtn.setBackgroundResource(R.drawable.onboarding_next_btn)
                    binding.nextBtn.text = "다음"
                }
            }
        })

        binding.nextBtn.setOnClickListener {
            if(binding.pager.currentItem == 2){
                val intent = Intent(this,HomeActivity::class.java)
                startActivity(intent)
            }
            else{
                val cur = binding.pager.currentItem
                binding.pager.currentItem = cur +1
            }
        }
    }
}