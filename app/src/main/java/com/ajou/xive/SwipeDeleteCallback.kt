package com.ajou.xive

import android.content.Context
import android.graphics.Canvas
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ajou.xive.home.TicketViewModel
import com.ajou.xive.home.adapter.CalendarTicketRVAdapter
import com.ajou.xive.home.model.Ticket

class SwipeDeleteCallback(
    val context: Context,
    private val recyclerView: RecyclerView,
) : ItemTouchHelper.Callback() {

    var adapter: CalendarTicketRVAdapter = (recyclerView.adapter as CalendarTicketRVAdapter)

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(0, ItemTouchHelper.LEFT)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (direction == ItemTouchHelper.LEFT) {
            val index = viewHolder.bindingAdapterPosition
            adapter.removeItem(index)
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val viewItem = viewHolder.itemView
            SwipeBackground.paintDrawCommandToStart(
                canvas,
                viewItem,
                R.drawable.calendar_bottomsheet_remove,
                R.color.error,
                dX
            )
        }
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}