package com.ajou.xive

interface DataSelection {
    fun getSelectedTicketId(id:Int, position: Int)

    fun getSelectedTicketUrl(url:String, eventId: Int)
}