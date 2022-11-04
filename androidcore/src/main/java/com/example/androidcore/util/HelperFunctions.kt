package com.example.androidcore.util

import java.util.*

fun String.titleCase(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}