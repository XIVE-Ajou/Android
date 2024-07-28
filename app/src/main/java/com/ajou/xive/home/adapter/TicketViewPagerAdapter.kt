package com.ajou.xive.home.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ajou.xive.DataSelection
import com.ajou.xive.R
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

            if (data.isXive) {
                binding.isXiveLogo.visibility = View.VISIBLE
                binding.isXiveText.visibility = View.VISIBLE
                if (data.isPurchase){
                    binding.isPurchaseText.text = context.getString(R.string.ticket_is_purchase)
                    binding.isPurchaseFrame.visibility = View.VISIBLE
                    binding.isPurchaseText.visibility = View.VISIBLE
                } else {
                    binding.isPurchaseText.text = context.getString(R.string.ticket_is_not_purchase)
                }
            } else {
                binding.isXiveLogo.visibility = View.GONE
                binding.isXiveText.visibility = View.GONE
                binding.isPurchaseFrame.visibility = View.GONE
                binding.isPurchaseText.visibility = View.GONE
            }

            binding.eventName.text = data.eventName
            binding.eventPeriod.text = String.format(context.getString(R.string.ticket_period), data.startDate, data.endDate)
            binding.eventPlace.text = data.eventPlace

            binding.ticket.setOnClickListener {
                link.getSelectedTicketData(data.eventWebUrl, data.eventId, data.ticketId)
            }

//            binding.ticket.setBackgroundColor(Color.GREEN)
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