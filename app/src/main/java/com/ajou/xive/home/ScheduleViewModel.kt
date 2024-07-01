package com.ajou.xive.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajou.xive.UserDataStore
import com.ajou.xive.home.model.Schedule
import com.ajou.xive.home.model.Ticket
import com.ajou.xive.network.RetrofitInstance
import com.ajou.xive.network.api.TicketService
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ScheduleViewModel : ViewModel() {
    private var accessToken : String? = null
    private var refreshToken : String? = null
    private val dataStore = UserDataStore()
    private val ticketService = RetrofitInstance.getInstance().create(TicketService::class.java)
    var isError = MutableLiveData<Boolean>(false)

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        isError.value = true
    }

    private val _schedule = MutableLiveData<Schedule>()
    val schedule : LiveData<Schedule>
        get() = _schedule

    private val _scheduleTickets = MutableLiveData<MutableList<Ticket>>()
    val scheduleTickets : LiveData<MutableList<Ticket>>
        get() = _scheduleTickets

    fun setSchedules(data: Schedule) {
        _schedule.postValue(data)
    }


    fun getTicketsFromSchedule(ticketIdList: List<Int>){
        viewModelScope.launch(exceptionHandler) {
            accessToken = dataStore.getAccessToken().toString()
            refreshToken = dataStore.getRefreshToken().toString()
            val list = mutableListOf<Ticket>()
            for (ticketId in ticketIdList) {
                val oneTicketDeferred = async { ticketService.getOneTicket(accessToken!!, refreshToken!!, ticketId.toString())  }
                val oneTicketResponse = oneTicketDeferred.await()
                if (oneTicketResponse.isSuccessful) {
                    list.add(oneTicketResponse.body()!!)
                }
            }
            _scheduleTickets.value = list
        }
    }
}