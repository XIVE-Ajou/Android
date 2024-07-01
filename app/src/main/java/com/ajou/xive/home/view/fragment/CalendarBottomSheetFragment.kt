package com.ajou.xive.home.view.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajou.xive.R
import com.ajou.xive.SwipeDeleteCallback
import com.ajou.xive.databinding.FragmentCalendarBottomSheetBinding
import com.ajou.xive.format
import com.ajou.xive.home.ScheduleViewModel
import com.ajou.xive.home.adapter.CalendarTicketRVAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate

class CalendarBottomSheetFragment : BottomSheetDialogFragment() {
    private var mContext : Context? = null
    private var _binding : FragmentCalendarBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel : ScheduleViewModel
    private lateinit var adapter : CalendarTicketRVAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        adapter = CalendarTicketRVAdapter(mContext!!, mutableListOf())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBottomSheetBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[ScheduleViewModel::class.java]

        val date = LocalDate.parse(viewModel.schedule.value!!.eventDay, format)
        binding.date.text = String.format(getString(R.string.calendar_popup_title),date.monthValue,date.dayOfMonth)

        binding.ticketRV.adapter = adapter
        binding.ticketRV.layoutManager = LinearLayoutManager(mContext)
        viewModel.getTicketsFromSchedule(viewModel.schedule.value!!.ticketId)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(mContext!!, R.style.CustomDialog)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            setupRatio(bottomSheetDialog)
            dialog.setCanceledOnTouchOutside(true)
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.scheduleTickets.observe(viewLifecycleOwner, Observer {
            if (viewModel.scheduleTickets.value!!.isNotEmpty()) {
                adapter.updateList(viewModel.scheduleTickets.value!!)
                val itemTouchHelper = ItemTouchHelper(SwipeDeleteCallback(mContext!!,binding.ticketRV))
                itemTouchHelper.attachToRecyclerView(binding.ticketRV)
            }
        })
    }

    private fun setupRatio(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from<View>(bottomSheet)
        val layoutParams = bottomSheet!!.layoutParams
        layoutParams.height = getBottomSheetDialogDefaultHeight()
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun getBottomSheetDialogDefaultHeight(): Int {
        return getWindowHeight() * 44 / 100
    }

    private fun getWindowHeight(): Int {
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }
    override fun onPause() {
        super.onPause()
        dialog?.dismiss()
    }
}