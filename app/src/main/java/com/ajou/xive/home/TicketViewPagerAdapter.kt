package com.ajou.xive.home

import android.R.id.text1
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import com.ajou.xive.DataSelection
import com.ajou.xive.R
import com.ajou.xive.databinding.ItemTicketBinding
import com.ajou.xive.home.model.Ticket
import com.bumptech.glide.Glide


class TicketViewPagerAdapter(val context : Context, var list: List<Ticket>, val link : DataSelection):RecyclerView.Adapter<TicketViewPagerAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTicketBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data:Ticket) {
            Glide.with(context)
                .load(data.eventImageUrl)
                .centerCrop()
                .into(binding.img)

//            Glide.with(context)
//                .load(context.resources.getDrawable(R.drawable.ticket_ex))
//                .into(binding.img)
            binding.ticket.setOnClickListener {
                link.getSelectedTicketUrl(data.eventWebUrl)

            }

            binding.ticket.setOnLongClickListener {
                link.getSelectedTicketId(data.ticketId,bindingAdapterPosition)
                return@setOnLongClickListener(true)

            }
//            binding.ticket.setOnClickListener {
//                link.getSelectedTicketUrl(data.eventWebUrl)
//            }

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

    fun updateList(newList : List<Ticket>){
        list = newList
        this.notifyDataSetChanged()
    }

    fun addToList(newList: List<Ticket>){
        list = newList
        this.notifyItemInserted(list.size-1)
    }

    fun removeAtList(newList: List<Ticket>, position: Int){
        list = newList
        this.notifyItemRemoved(position)
    }

}