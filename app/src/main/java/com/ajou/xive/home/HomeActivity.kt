package com.ajou.xive.home

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ajou.xive.R
import com.ajou.xive.UserDataStore
import com.ajou.xive.databinding.ActivityHomeBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private var _binding : ActivityHomeBinding? = null
    private val binding get() = _binding!!
    private val dataStore = UserDataStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
            val test = dataStore.getAccessToken()
            Log.d("nonmember token test",test.toString())
        }
        val tmpList = listOf<String>("ex1","ex2","ex3","ex4")
        val adapter = TicketViewPagerAdapter(this,tmpList)

        binding.ticketVP.adapter = adapter
        binding.ticketVP.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        binding.indicator.attachToPager(binding.ticketVP)

        binding.ticketVP.offscreenPageLimit = 4
        // item_view 간의 양 옆 여백을 상쇄할 값
        val offsetBetweenPages = resources.getDimensionPixelOffset(R.dimen.offsetBetweenPages).toFloat()

        binding.ticketVP.addItemDecoration(object: RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.right = offsetBetweenPages.toInt()
                outRect.left = offsetBetweenPages.toInt()
            }
        })
        binding.ticketVP.setPageTransformer { page, position ->
            val myOffset = position * -(6*offsetBetweenPages)
            if (position < -1) {
                page.translationX = -myOffset
            }
            else if (position <= 1) {
                // Paging 시 Y축 Animation 배경색을 약간 연하게 처리
                val scaleFactor = 0.8f.coerceAtLeast(1 - kotlin.math.abs(position))
                page.translationX = myOffset
                page.scaleY = scaleFactor
                page.alpha = scaleFactor
            } else {
                page.alpha = 0f
                page.translationX = myOffset
            }
        }

        val multiOptions = RequestOptions().transform(
            FitCenter(),
            BlurTransformation(25,3)
        )
        Glide.with(this)
            .load(this.getDrawable(R.drawable.ticket_ex))
            .apply(multiOptions)
            .into(binding.bgImg)
//        binding.ticketVP.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
//            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
//                Glide.with(this@HomeActivity)
//                    .load(resources.getDrawable(R.drawable.genreballad))
//                    .thumbnail(Glide.with(this@HomeActivity).load(resources.getDrawable(R.drawable.genreballad)).override(100, 100)) // 추후에 깜빡임 심할 시에 넣기
//                    .apply(multiOptions)
//                    .into(binding.bgImg)
//            }
//        })
    }
     // https://blog.gangnamunni.com/post/viewpager2/
}