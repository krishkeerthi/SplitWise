package com.example.splitwise.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

fun nameCheck(value: String): Boolean {
    return value.matches("[a-zA-Z0-9-&\"'.\n /,]+$".toRegex())
}

fun formatDate(date: Date): String{
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.UK) // warning shown to add locale
    return sdf.format(date).toString()
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