package com.ajou.xive

interface DataSelection {
    fun getSelectedTicketId(id:Int, position: Int)

    fun getSelectedTicketData(url:String, eventId: Int, ticketId: Int, isVisited: Boolean)
}