package com.example.splitwise.util

import java.text.SimpleDateFormat
import java.util.*

fun nameCheck(value: String): Boolean {
    return value.matches("[a-zA-Z0-9-&\"'.\n /,]+$".toRegex())
}

fun formatDate(date: Date): String{
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.UK) // warning shown to add locale
    return sdf.format(date).toString()

}