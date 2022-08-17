package com.example.splitwise.util

import android.content.ContentValues
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.splitwise.R
import java.net.URL
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

fun getCategoryDrawableResource(category: Int): Int {
    return when(category){
        Category.FOOD.ordinal -> R.drawable.food
        Category.OTHERS.ordinal -> R.drawable.others
        Category.TRAVEL.ordinal -> R.drawable.travel
        Category.RENT.ordinal -> R.drawable.rent
        Category.FEES.ordinal -> R.drawable.fees
        Category.REPAIRS.ordinal -> R.drawable.repair
        Category.ESSENTIALS.ordinal -> R.drawable.essentials
        Category.ENTERTAINMENT.ordinal -> R.drawable.entertainment
        Category.TICKETS.ordinal -> R.drawable.tickets
        else -> R.drawable.others
    }
}

fun getDateFilterDrawableResource(dateFilter: Int): Int {
    return when(dateFilter){
        DateFilter.AFTER.ordinal -> R.drawable.after
        DateFilter.BEFORE.ordinal -> R.drawable.before
        else -> R.drawable.before
    }
}

fun getAmountFilterDrawableResource(amountFilter: Int): Int {
    return when(amountFilter){
        AmountFilter.BELOW.ordinal -> R.drawable.below
        AmountFilter.ABOVE.ordinal -> R.drawable.above
        else -> R.drawable.above
    }
}

fun getGroupFilterDrawableResource(groupFilter: Int): Int {
    return when(groupFilter){
        GroupFilter.DATE.ordinal -> R.drawable.date
        GroupFilter.AMOUNT.ordinal -> R.drawable.others
        else -> R.drawable.others
    }
}

fun downloadBitmap(imageUrl: String): Bitmap? {
    return try {
        val conn = URL(imageUrl).openConnection()
        conn.connect()
        val inputStream = conn.getInputStream()
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        bitmap
    } catch (e: Exception) {
        Log.e(ContentValues.TAG, "Exception $e")
        null
    }
}
