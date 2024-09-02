package com.ajou.xive.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajou.xive.UserDataStore
import com.ajou.xive.format
import com.ajou.xive.home.model.Schedule
import com.ajou.xive.home.model.Ticket
import com.ajou.xive.network.RetrofitInstance
import com.ajou.xive.network.api.SchedulesService
import com.ajou.xive.network.api.TicketService
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.YearMonth

class ScheduleViewModel : ViewModel() {
    private var accessToken : String? = null
    private var refreshToken : String? = null
    private val dataStore = UserDataStore()
    private val ticketService = RetrofitInstance.getInstance().create(TicketService::class.java)
    private val schedulesService = RetrofitInstance.getInstance().create(SchedulesService::class.java)
    var isError = MutableLiveData<Boolean>(false)

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        isError.value = true
    }

    private val _schedule = MutableLiveData<Schedule>()
    val schedule : LiveData<Schedule>
        get() = _schedule

    private val _scheduleList = MutableLiveData<List<Schedule>>(mutableListOf())
    val scheduleList : LiveData<List<Schedule>>
        get() = _scheduleList

    private val _scheduleTickets = MutableLiveData<MutableList<Ticket>>()
    val scheduleTickets : LiveData<MutableList<Ticket>>
        get() = _scheduleTickets

    private val _selectedDate = MutableLiveData<String?>()
    val selectedDate : LiveData<String?>
        get() = _selectedDate

    init {
        val today = LocalDate.now()
        getMonthSchedules(today.yearMonth)
    }

    fun setSchedules(data: Schedule) {
        _schedule.postValue(data)
    }

    fun deleteSchedule(index: Int) {
        val currentList = _scheduleTickets.value?.toMutableList() ?: mutableListOf()
        val removedItemId = currentList[index].ticketId
        val list = _scheduleList.value!!.mapNotNull { schedule ->
            val filteredTicketId = schedule.ticketId.filter { it != removedItemId }
            if (filteredTicketId.isEmpty()) {
                null
            } else {
                schedule.copy(ticketId = filteredTicketId)
            }
        }
        _scheduleList.postValue(list) // 달의 전체 스케줄 관리

        currentList.removeAt(index)
        _scheduleTickets.postValue(currentList) // 하루의 스케줄 관리
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
            _scheduleTickets.postValue(list)
        }
    }

    fun setSelectedDate(date: String?) {
        _selectedDate.postValue(date)
    }

    fun setScheduleList(newList:List<Schedule>) {
        _scheduleList.postValue(newList as MutableList<Schedule>)
    }

    fun getMonthSchedules(currentMonth: YearMonth) {
        CoroutineScope(Dispatchers.IO).launch {
            accessToken = dataStore.getAccessToken().toString()
            refreshToken = dataStore.getRefreshToken().toString()
            val scheduleDeferred = async { schedulesService.getMonthSchedules(accessToken!!, refreshToken!!,currentMonth.toString()) }
            val scheduleResponse = scheduleDeferred.await()
            if (scheduleResponse.isSuccessful) {
                setScheduleList(scheduleResponse.body()!!.data)
            }
        }
    }
}