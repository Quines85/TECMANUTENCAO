package com.tecmanutencao.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault())

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }
}
