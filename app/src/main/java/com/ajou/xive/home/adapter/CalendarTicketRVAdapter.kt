package com.ajou.xive.home.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ajou.xive.DataSelection
import com.ajou.xive.databinding.ItemCalendarEventBinding
import com.ajou.xive.home.model.Ticket
import com.bumptech.glide.Glide

class CalendarTicketRVAdapter(val context: Context, var list: List<Ticket>, val link: DataSelection): RecyclerView.Adapter<CalendarTicketRVAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCalendarEventBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data:Ticket){
            Glide.with(context)
                .load(data.eventImageUrl)
                .centerCrop()
                .into(binding.img)

            binding.location.text = data.eventPlace
            val period = "${data.startDate} ~ ${data.endDate}"
            binding.period.text = period
            binding.title.text = data.eventName // TODO eventRound도 같이 띄워줘야하는지
            binding.type.text = data.eventType

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemCalendarEventBinding.inflate(LayoutInflater.from(context),parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateList(newList: List<Ticket>) {
        list = newList
        notifyDataSetChanged()
    }

    fun removeItem(index: Int){
        val tmpList : MutableList<Ticket> = list.toList().toMutableList()
        tmpList.removeAt(index)
        link.getSelectedTicketId(list[index].ticketId,index)
        list = tmpList
        notifyItemRemoved(index)
    }
}