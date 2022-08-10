package com.example.splitwise.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import java.text.SimpleDateFormat
import java.util.*

fun nameCheck(value: String): Boolean {
    return value.matches("[a-zA-Z0-9-&\"'.\n /,]+$".toRegex())
}

fun formatDate(date: Date): String{
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.UK) // warning shown to add locale

    val currentDate = Calendar.getInstance()
    val currentYear = currentDate[Calendar.YEAR]
    val currentMonth = currentDate[Calendar.MONTH]
    val currentDay = currentDate[Calendar.DAY_OF_MONTH]

    val createdDate = Calendar.getInstance()
    createdDate.time = date
    val year = createdDate[Calendar.YEAR]
    val month = createdDate[Calendar.MONTH]
    val day = createdDate[Calendar.DAY_OF_MONTH]

    return if((currentYear == year) && (currentMonth == month)){
        when {
            currentDay == day -> "Today"
            day == (currentDay - 1) -> "Yesterday"
            else -> sdf.format(date).toString()
        }
    }
    else
        sdf.format(date).toString()
}

fun Float.roundOff(): String{
    return String.format("%.2f", this)
}

fun String.titleCase(): String{
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

fun Context.isDarkThemeOn(): Boolean {
    return resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
}