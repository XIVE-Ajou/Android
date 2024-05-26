package com.ajou.xive.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ajou.xive.home.model.Ticket

class TicketViewModel : ViewModel() {

    private val _ticketList = MutableLiveData<MutableList<Ticket>>()
    val ticketList : LiveData<MutableList<Ticket>>
    get() = _ticketList

    private val _nfcUrl = MutableLiveData<String>()
    val nfcUrl : LiveData<String>
    get() = _nfcUrl

    private val _ticket = MutableLiveData<Ticket>()
    val ticket : LiveData<Ticket>
    get() = _ticket

    private val _type = MutableLiveData<String>()
    val type : LiveData<String>
    get() = _type

    fun setNfcUrl(data : String) {
        _nfcUrl.postValue(data)
    }

    fun setTicket(data:Ticket){
        _ticket.postValue(data)
    }

    fun setTicketList(data: List<Ticket>){
        _ticketList.postValue(data as MutableList<Ticket>?)
    }

    fun setType(data: String){
        _type.postValue(data)
    }

}