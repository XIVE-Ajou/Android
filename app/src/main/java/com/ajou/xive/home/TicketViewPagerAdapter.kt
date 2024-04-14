package com.ajou.xive.home

import android.R.id.text1
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import com.ajou.xive.R
import com.ajou.xive.databinding.ItemTicketBinding
import com.bumptech.glide.Glide


class TicketViewPagerAdapter(val context : Context, val list: List<String>):RecyclerView.Adapter<TicketViewPagerAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTicketBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(url:String) {
            // TODO glide로 이미지 삽입
            Glide.with(context)
                .load(context.resources.getDrawable(R.drawable.ticket_ex))
                .centerCrop()
                .into(binding.img)
            // TODO 클릭 시 세부 화면 이동(웹뷰 예정, 관련 데이터만 전달하면 될듯)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TicketViewPagerAdapter.ViewHolder {
        val binding = ItemTicketBinding.inflate(LayoutInflater.from(context),parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketViewPagerAdapter.ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}