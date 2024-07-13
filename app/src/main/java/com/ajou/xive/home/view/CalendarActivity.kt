package com.ajou.xive.home.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.ajou.xive.R
import com.ajou.xive.databinding.ActivityCalendarBinding
import com.ajou.xive.databinding.CalendarDayBinding
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import com.ajou.xive.displayText
import com.ajou.xive.format
import com.ajou.xive.home.ScheduleViewModel
import com.ajou.xive.home.view.fragment.CalendarBottomSheetFragment
import com.bumptech.glide.Glide
import com.kizitonwose.calendar.core.*
import java.time.DayOfWeek

class CalendarActivity : AppCompatActivity() {
    private var _binding: ActivityCalendarBinding? = null
    private val binding get() = _binding!!
    private val viewModel : ScheduleViewModel by viewModels()
    private var selectedDate = LocalDate.now()
    private var currentMonth = YearMonth.now()
    private val startMonth = YearMonth.of(2024, 1) // 2024년 1월부터 제공
    private val endMonth = currentMonth  // 현재는 다음 달이 필요 없어서 endMonth와 currentMonth가 동일
    private var isFirst = true
    private var isEdit = false
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

        viewModel.scheduleList.observe(this, Observer {
            if (viewModel.scheduleList.value!!.isNotEmpty()){
                binding.calendar.post {
                    viewModel.scheduleList.value!!.map { binding.calendar.notifyDateChanged(LocalDate.parse(it.eventDay, format)) }
                }
            }
        })

        viewModel.scheduleTickets.observe(this, Observer {
            if (viewModel.selectedDate.value != null) {
                val element = viewModel.scheduleTickets.value!!.find { it.eventDay == viewModel.selectedDate.value }
                if (element == null) { // 완전히 사라졌을 때만 처리가능.. -> 여러 개의 티켓이 있고 하나를 삭제했을 때는 숫자 변경 X
                    isEdit = true
                    binding.calendar.notifyDateChanged(LocalDate.parse(viewModel.selectedDate.value, format))
                }
            }
        })
    }

    private fun updateTitle() {
        isFirst = true
        val month = binding.calendar.findFirstVisibleMonth()?.yearMonth ?: return
        val year = month.year.toString() +"년"
        currentMonth = month
        viewModel.getMonthSchedules(currentMonth)
        binding.year.text = year
        binding.month.text = month.month.displayText(short = false)
        binding.monthMinus.isEnabled = month > startMonth
        binding.monthPlus.isEnabled = month < endMonth
        updateDayWeekColor()
    }

    private fun updateDayWeekColor() {
        val today = LocalDate.now()
        if (today == selectedDate && "${today.monthValue}월" == binding.month.text) {
            for ((index, dayText) in daysOfWeek.withIndex()) {
                val dayLayout = binding.dayWeek.getChildAt(index)
                if (dayLayout!=null){
                    val textView: TextView? = dayLayout.findViewById(R.id.dayWeekText)
                    if (textView != null) {
                        textView.text = dayText.displayText()
                        if (selectedDate.dayOfWeek == daysOfWeek[index]) {
                            textView.setTextColor(getColor(R.color.primary))
                        } else {
                            textView.setTextColor(getColor(R.color.gray100))
                        }
                    }
                }
            }
        } else {
            for (i in 0..6) {
                binding.dayWeek.getChildAt(i).findViewById<TextView>(R.id.dayWeekText).setTextColor(getColor(R.color.gray100))
            }
        }
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

        val element = viewModel.scheduleList.value!!.find { LocalDate.parse(it.eventDay, format) == date }
        if (element != null) {
            viewModel.setSchedules(element)
            viewModel.setSelectedDate(element.eventDay)
            val dialog = CalendarBottomSheetFragment()
            dialog.show(supportFragmentManager, "schedule")
        } else {
            viewModel.setSelectedDate(null)
        }
    }

    private fun bindDate(date: LocalDate, dayText: TextView, dayBg: ImageView, img: ImageView, imgBg: ImageView, ticketCount: TextView, ticketCountBg: View, isSelectable: Boolean) {
        dayText.text = date.dayOfMonth.toString()
        if (isSelectable) {
//            if (isFirst || isEdit) {
//                val element = viewModel.scheduleList.value!!.find { LocalDate.parse(it.eventDay, format) == date }
//                if (element != null){
//                    img.visibility = View.VISIBLE
//                    if (element.eventImageUrl != null && LocalDate.parse(element.eventDay, format) == date){
//                        Glide.with(this)
//                            .load(element.eventImageUrl)
//                            .centerCrop()
//                            .into(img)
//                        if (element.ticketId.size > 1){
//                            ticketCount.text = element.ticketId.size.toString()
//                            ticketCountBg.visibility = View.VISIBLE
//                            ticketCount.visibility = View.VISIBLE
//                        } else if (element.ticketId.size == 1) {
//                            ticketCount.visibility = View.GONE
//                            ticketCountBg.visibility = View.GONE
//                        }
//                    }
//                } else {
//                    img.visibility = View.GONE
//                    imgBg.visibility = View.GONE
//                    ticketCount.visibility = View.GONE
//                    ticketCountBg.visibility = View.GONE
//                }
//                isEdit = false
//            }
            val element = viewModel.scheduleList.value!!.find { LocalDate.parse(it.eventDay, format) == date }
            if (element != null){
                img.visibility = View.VISIBLE
                if (element.eventImageUrl != null && LocalDate.parse(element.eventDay, format) == date){
                    Glide.with(this)
                        .load(element.eventImageUrl)
                        .centerCrop()
                        .into(img)
                    if (element.ticketId.size > 1){
                        ticketCount.text = element.ticketId.size.toString()
                        ticketCountBg.visibility = View.VISIBLE
                        ticketCount.visibility = View.VISIBLE
                    } else if (element.ticketId.size == 1) {
                        ticketCount.visibility = View.GONE
                        ticketCountBg.visibility = View.GONE
                    }
                }
            } else {
                img.visibility = View.GONE
                imgBg.visibility = View.GONE
                ticketCount.visibility = View.GONE
                ticketCountBg.visibility = View.GONE
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
}