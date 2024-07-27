package com.ajou.xive.home.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ajou.xive.DataSelection
import com.ajou.xive.databinding.ItemTicketBinding
import com.ajou.xive.home.model.Ticket
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class TicketViewPagerAdapter(val context : Context, var list: List<Ticket>, val link : DataSelection):RecyclerView.Adapter<TicketViewPagerAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemTicketBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data:Ticket) {
            Glide.with(context)
                .load(data.eventImageUrl)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.img)

            binding.ticket.setOnClickListener {
                link.getSelectedTicketData(data.eventWebUrl, data.eventId, data.ticketId)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemTicketBinding.inflate(LayoutInflater.from(context),parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateList(newList : List<Ticket>){
        list = newList
        this.notifyDataSetChanged()
    }

    fun addToList(newList: List<Ticket>){
        list = newList
        this.notifyItemInserted(list.size-1)
    }

}