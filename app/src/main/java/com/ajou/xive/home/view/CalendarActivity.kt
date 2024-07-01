package com.ajou.xive.home.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import com.ajou.xive.R
import com.ajou.xive.UserDataStore
import com.ajou.xive.databinding.ActivityCalendarBinding
import com.ajou.xive.databinding.CalendarDayBinding
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import com.ajou.xive.displayText
import com.ajou.xive.format
import com.ajou.xive.home.ScheduleViewModel
import com.ajou.xive.home.model.Schedule
import com.ajou.xive.home.view.fragment.CalendarBottomSheetFragment
import com.ajou.xive.network.RetrofitInstance
import com.ajou.xive.network.api.SchedulesService
import com.bumptech.glide.Glide
import com.kizitonwose.calendar.core.*
import kotlinx.coroutines.*
import java.time.DayOfWeek

class CalendarActivity : AppCompatActivity() {
    private var _binding: ActivityCalendarBinding? = null
    private val binding get() = _binding!!
    private val viewModel : ScheduleViewModel by viewModels()

    private val schedulesService = RetrofitInstance.getInstance().create(SchedulesService::class.java)
    private var selectedDate = LocalDate.now()
    private var currentMonth = YearMonth.now()
    private val startMonth = YearMonth.of(2024, 1) // 2024년 1월부터 제공
    private val endMonth = currentMonth  // 현재는 다음 달이 필요 없어서 endMonth와 currentMonth가 동일
    private lateinit var accessToken : String
    private lateinit var refreshToken : String
    private val dataStore = UserDataStore()
    private var schedulesList : List<Schedule> = emptyList()
    private var isFirst = true
    private val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val dayBinding = CalendarDayBinding.bind(view)
            val dayText = dayBinding.day
            val dayBg = dayBinding.dayBg
            val img = dayBinding.img
            val imgBg = dayBinding.imgBg
            val ticketCount = dayBinding.ticketCount
            val ticketCountBg = dayBinding.ticketCountBg

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        dateClicked(date = day.date)
                    }
                }
            }
        }
        binding.calendar.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                bindDate(data.date, container.dayText, container.dayBg, container.img, container.imgBg, container.ticketCount, container.ticketCountBg, data.position == DayPosition.MonthDate)
            }
            override fun create(view: View): DayViewContainer = DayViewContainer(view)
        }

        binding.calendar.monthScrollListener = { updateTitle() }
        binding.calendar.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendar.scrollToMonth(currentMonth)

        for ((index, dayText) in daysOfWeek.withIndex()) {
            val dayLayout = binding.dayWeek.getChildAt(index)
            if (dayLayout!=null){
                val textView: TextView? = dayLayout.findViewById(R.id.dayWeekText)
                if (textView != null) {
                    textView.text = dayText.displayText()
                    if (selectedDate.dayOfWeek == daysOfWeek[index]) {
                        textView.setTextColor(getColor(R.color.primary))
                    }
                }
            }
        }
//        binding.calendar.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
//            override fun bind(container: MonthViewContainer, data: CalendarMonth) {
//                if (container.titlesContainer.tag == null) {
//                    container.titlesContainer.tag = data.yearMonth
//                    container.titlesContainer.children.map { it as TextView }
//                        .forEachIndexed { index, textView ->
//                            textView.text = daysOfWeek[index].displayText()
//                            if (selectedDate.dayOfWeek == daysOfWeek[index]) {
//                                textView.setTextColor(getColor(R.color.primary))
//                            }
//                        }
//                }
//            }
//            override fun create(view: View): MonthViewContainer = MonthViewContainer(view)
//        }

        binding.monthPlus.setOnClickListener {
            val prevMonth = binding.calendar.findFirstVisibleMonth()?.yearMonth
            val month = prevMonth!!.plusMonths(1)
            binding.calendar.smoothScrollToMonth(month)
        }
        binding.monthMinus.setOnClickListener {
            val prevMonth = binding.calendar.findFirstVisibleMonth()?.yearMonth
            val month = prevMonth!!.minusMonths(1)
            binding.calendar.smoothScrollToMonth(month)
        }
    }

    class MonthViewContainer(view: View) : ViewContainer(view) {
        val titlesContainer = view as ViewGroup
    }

    private fun updateTitle() {
        isFirst = true
        val month = binding.calendar.findFirstVisibleMonth()?.yearMonth ?: return
        val year = month.year.toString() +"년"
        currentMonth = month
        getMonthSchedules()
        binding.year.text = year
        binding.month.text = month.month.displayText(short = false)
        binding.monthMinus.isEnabled = month > startMonth
        binding.monthPlus.isEnabled = month < endMonth

    }

    private fun dateClicked(date: LocalDate) {
        isFirst = false
        binding.calendar.notifyDateChanged(selectedDate) // 이전 선택값 해제
        for (i in 0..6){
            if (selectedDate.dayOfWeek == daysOfWeek[i]) {
                binding.dayWeek.getChildAt(i).findViewById<TextView>(R.id.dayWeekText).setTextColor(getColor(R.color.gray100))
            }
            else if (date.dayOfWeek == daysOfWeek[i]) {
                binding.dayWeek.getChildAt(i).findViewById<TextView>(R.id.dayWeekText).setTextColor(getColor(R.color.primary))
            }
        }

        selectedDate = date
        binding.calendar.notifyDateChanged(date) // 새로운 선택값

        val element = schedulesList.find { LocalDate.parse(it.eventDay, format) == date }
        if (element != null) {
            viewModel.setSchedules(element)
            val dialog = CalendarBottomSheetFragment()
            dialog.show(supportFragmentManager, "schedule")
        }

        // TODO 이미지가 있는 값일 경우 bottomsheet 띄워야함
    }

    private fun bindDate(date: LocalDate, dayText: TextView, dayBg: ImageView, img: ImageView, imgBg: ImageView, ticketCount: TextView, ticketCountBg: View, isSelectable: Boolean) {
        dayText.text = date.dayOfMonth.toString()
        if (isSelectable) {
            if (isFirst) {
                val element = schedulesList.find { LocalDate.parse(it.eventDay, format) == date }
                if (element != null){
                    img.visibility = View.VISIBLE
                    if (element.eventImageUrl != null && LocalDate.parse(element.eventDay, format) == date){
                        Glide.with(this)
                            .load(element.eventImageUrl)
                            .into(img)
                        if (element.ticketId.size > 1){
                            ticketCount.text = element.ticketId.size.toString()
                            ticketCountBg.visibility = View.VISIBLE
                            ticketCount.visibility = View.VISIBLE
                        } else {
                            ticketCount.visibility = View.GONE
                            ticketCountBg.visibility = View.GONE
                        }
                    }
                } else {
                    img.visibility = View.GONE
                    ticketCount.visibility = View.GONE
                    ticketCountBg.visibility = View.GONE
                }
            }
            when {
                date == selectedDate -> {
                    if (img.visibility == View.VISIBLE) {
                        imgBg.visibility = View.VISIBLE
                    } else {
                        dayText.setTextColor(resources.getColor(R.color.white))
                        dayBg.setBackgroundResource(R.drawable.calendar_selected_bg)
                    }
                }
                else -> {
                    if (img.visibility == View.VISIBLE) {
                        imgBg.visibility = View.GONE
                    } else {
                        dayText.setTextColor(resources.getColor(R.color.black))
                        dayBg.background = null
                    }
                }
            }
        } else {
            dayText.setTextColor(resources.getColor(R.color.gray100))
            dayBg.background = null
            imgBg.visibility = View.GONE
        }
    }

    private fun getMonthSchedules() {
        CoroutineScope(Dispatchers.IO).launch {
            accessToken = dataStore.getAccessToken().toString()
            refreshToken = dataStore.getRefreshToken().toString()
            val scheduleDeferred = async { schedulesService.getMonthSchedules(accessToken, refreshToken,currentMonth.toString()) }
            val scheduleResponse = scheduleDeferred.await()
            if (scheduleResponse.isSuccessful) {
                Log.d("scheduleResponse", scheduleResponse.body()?.data.toString())
                schedulesList = scheduleResponse.body()?.data!!
                withContext(Dispatchers.Main) {
                    binding.calendar.post {
                        schedulesList.map { binding.calendar.notifyDateChanged(LocalDate.parse(it.eventDay, format)) }
                    }
                }
            }
        }
    }
}