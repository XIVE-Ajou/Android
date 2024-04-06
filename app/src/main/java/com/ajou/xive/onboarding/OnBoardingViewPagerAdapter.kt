package com.ajou.xive.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ajou.xive.onboarding.view.fragment.SwipeFragment1
import com.ajou.xive.onboarding.view.fragment.SwipeFragment2
import com.ajou.xive.onboarding.view.fragment.SwipeFragment3

class OnBoardingViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    private val fragments = listOf<Fragment>(
        SwipeFragment1(),
        SwipeFragment2(),
        SwipeFragment3()
    )

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}