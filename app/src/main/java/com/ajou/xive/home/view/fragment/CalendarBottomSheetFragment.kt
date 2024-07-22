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
import com.ajou.xive.*
import com.ajou.xive.databinding.FragmentCalendarBottomSheetBinding
import com.ajou.xive.home.ScheduleViewModel
import com.ajou.xive.home.adapter.CalendarTicketRVAdapter
import com.ajou.xive.network.RetrofitInstance
import com.ajou.xive.network.api.TicketService
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalendarBottomSheetFragment : BottomSheetDialogFragment(), DataSelection {
    private var mContext : Context? = null
    private var _binding : FragmentCalendarBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel : ScheduleViewModel
    private lateinit var adapter : CalendarTicketRVAdapter
    private val dataStore = UserDataStore()
    private val ticketService = RetrofitInstance.getInstance().create(TicketService::class.java)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        adapter = CalendarTicketRVAdapter(mContext!!, mutableListOf(),this)
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
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        behavior.peekHeight = getBottomSheetDialogDefaultHeight()
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        bottomSheet.layoutParams = bottomSheet.layoutParams
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

    override fun getSelectedTicketId(id: Int, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val accessToken = dataStore.getAccessToken()
            val refreshToken = dataStore.getRefreshToken()
            val deleteDeferred =
                async { ticketService.deleteTicket(accessToken!!, refreshToken!!, id.toString()) }
            val deleteResponse = deleteDeferred.await()
            if (deleteResponse.isSuccessful) {
                if (viewModel.scheduleTickets.value!!.size == 1 ){
                    dialog?.dismiss()
                }
                viewModel.deleteSchedule(position)
            } else {
                Log.d("deleteResponse fail", deleteResponse.errorBody()?.string().toString())
            }
        }
    }

    override fun getSelectedTicketUrl(url: String, eventId: Int) {
    }
}