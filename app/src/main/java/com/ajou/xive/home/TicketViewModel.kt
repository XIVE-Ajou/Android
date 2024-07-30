package com.ajou.xive.home

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajou.xive.NetworkErrorActivity
import com.ajou.xive.UserDataStore
import com.ajou.xive.home.model.Schedule
import com.ajou.xive.home.model.Ticket
import com.ajou.xive.network.RetrofitInstance
import com.ajou.xive.network.api.TicketService
import kotlinx.coroutines.*

class TicketViewModel : ViewModel() {
    private val ticketService = RetrofitInstance.getInstance().create(TicketService::class.java)
    private val dataStore = UserDataStore()
    private var accessToken : String? = null
    private var refreshToken : String? = null
    var isError = MutableLiveData<Boolean>(false)

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

    private val _hasTicket = MutableLiveData<Boolean>()
    val hasTicket : LiveData<Boolean>
    get() = _hasTicket

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.d("error find exceptionHandler","")
        isError.value = true
    }
    init {
        getUsersTicket()
    }
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

    fun deleteTicket(index: Int) {
        _ticketList.value?.removeAt(index)
    }

    fun getUsersTicket(){
        viewModelScope.launch(exceptionHandler) {
            accessToken = dataStore.getAccessToken().toString()
            refreshToken = dataStore.getRefreshToken().toString()
            Log.d("error find token check","$accessToken $refreshToken")
            val ticketListDeferred =
                async { ticketService.getAllTickets(accessToken!!, refreshToken!!) }
            val ticketListResponse = ticketListDeferred.await()

            if (ticketListResponse.isSuccessful) {
                Log.d("user all ticket",ticketListResponse.body().toString())
                if (ticketListResponse.body()?.data!!.isEmpty()) {
                    Log.d("error find no ticket","")
                    _hasTicket.value = false
                }
                else {
                    _type.postValue("update")
                    val purchasedTicketList = ticketListResponse.body()?.data!!.filter { it.isPurchase }
                    Log.d("purchased Ticket","${_hasTicket.value}   $purchasedTicketList")
                    if (purchasedTicketList.isEmpty()) _hasTicket.value = false
                    else _hasTicket.value = true
//                    else if(_hasTicket.value != true && purchasedTicketList.isNotEmpty()) _hasTicket.value = true
                    setTicketList(purchasedTicketList)
                }
            }else{
                Log.d("ticketresponse fail",ticketListResponse.errorBody()?.string().toString())
            }
        }
    }
}