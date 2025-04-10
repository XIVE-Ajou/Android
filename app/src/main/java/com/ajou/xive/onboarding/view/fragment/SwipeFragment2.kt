package com.ajou.xive.onboarding.view.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ajou.xive.R
import com.ajou.xive.databinding.FragmentSwipe2Binding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class SwipeFragment2 : Fragment() {
    private var _binding : FragmentSwipe2Binding ?= null
    private val binding get() = _binding!!
    private var mContext : Context ?= null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSwipe2Binding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gifDrawable = pl.droidsonroids.gif.GifDrawable(mContext!!.resources, R.drawable.onboarding2)
        binding.gif.setImageDrawable(gifDrawable)
    }

    override fun onResume() {
        super.onResume()
        val gifDrawable = pl.droidsonroids.gif.GifDrawable(mContext!!.resources, R.drawable.onboarding2)
        binding.gif.setImageDrawable(gifDrawable)
    }


}