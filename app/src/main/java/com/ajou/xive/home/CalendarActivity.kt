package com.ajou.xive.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import com.ajou.xive.R
import com.ajou.xive.databinding.ActivityCalendarBinding
import com.ajou.xive.databinding.CalendarDayBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import com.ajou.xive.displayText
import java.time.DayOfWeek

class CalendarActivity : AppCompatActivity() {
    private var _binding: ActivityCalendarBinding? = null
    private val binding get() = _binding!!

//    private val selectedDates = mutableSetOf<LocalDate>()
    private var selectedDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)  // Adjust as needed
        val endMonth = currentMonth.plusMonths(100)  // Adjust as needed

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val dayText = CalendarDayBinding.bind(view).day
            val dayBg = CalendarDayBinding.bind(view).dayBg
            val img = CalendarDayBinding.bind(view).img
            val imgBg = CalendarDayBinding.bind(view).imgBg

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
                bindDate(data.date, container.dayText, container.dayBg, container.img, container.imgBg, data.position == DayPosition.MonthDate)
            }

            override fun create(view: View): DayViewContainer = DayViewContainer(view)
        }

        binding.calendar.monthScrollListener = { updateTitle() }
        binding.calendar.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendar.scrollToMonth(currentMonth)
        binding.calendar.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                if (container.titlesContainer.tag == null) {
                    container.titlesContainer.tag = data.yearMonth
                    container.titlesContainer.children.map { it as TextView }
                        .forEachIndexed { index, textView ->
                            textView.text = daysOfWeek[index].displayText()
                            textView.setTextColor(resources.getColor(R.color.black))
                        }
                }
            }

            override fun create(view: View): MonthViewContainer = MonthViewContainer(view)

        }

        binding.monthPlus.setOnClickListener {
            // TODO 클릭 시 +1 월
        }
        binding.monthMinus.setOnClickListener {
            // TODO 클릭 시 -1 월
        }
    }

    class MonthViewContainer(view: View) : ViewContainer(view) {
        val titlesContainer = view as ViewGroup
    }

    private fun updateTitle() {
        val month = binding.calendar.findFirstVisibleMonth()?.yearMonth ?: return
        val year = month.year.toString() +"년"
        binding.year.text = year
        binding.month.text = month.month.displayText(short = false)
    }

    private fun dateClicked(date: LocalDate) {
        binding.calendar.notifyDateChanged(selectedDate)
        selectedDate = date
        binding.calendar.notifyDateChanged(date)
    }

    private fun bindDate(date: LocalDate, dayText: TextView, dayBg: ImageView, img: ImageView, imgBg: ImageView, isSelectable: Boolean) {
        dayText.text = date.dayOfMonth.toString()
        if (isSelectable) {
            when {
                date == selectedDate -> {
                    dayText.setTextColor(resources.getColor(R.color.white))
                    dayBg.setBackgroundResource(R.drawable.calendar_selected_bg)
//                    imgBg.visibility = View.VISIBLE // TODO img 있을 경우에 dayBg 설정 대신 imgBg 설정

                }
                else -> {
                    dayText.setTextColor(resources.getColor(R.color.black))
                    dayBg.background = null
//                    imgBg.visibility = View.GONE
                }
            }
        } else {
            dayText.setTextColor(resources.getColor(R.color.gray300))
            dayBg.background = null
            imgBg.visibility = View.GONE
        }
    }
}