package com.example.splitwise.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.model.ExpenseMember
import com.example.splitwise.ui.activity.main.MainActivity
import com.google.android.material.internal.ContextUtils.getActivity
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


fun nameCheck(value: String): Boolean {
    return value.matches("[a-zA-Z0-9-&\"'.\n /,()]+$".toRegex())
}

fun numberCheck(number: Long): Boolean{
    return number in 1000000000..9999999999
}
fun formatDate(date: Date, dateOnly: Boolean = false): String {
    //val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.UK) // warning shown to add locale

    val sdf = SimpleDateFormat("dd-LLL-yyyy", Locale.UK)

    return if (dateOnly)
        sdf.format(date).toString()
    else
        when {
            (DateUtils.isToday(date.time)) -> "${DateUtils.getRelativeTimeSpanString(date.time, Date().time, 0)}"
            (DateUtils.isToday(date.time + DateUtils.DAY_IN_MILLIS)) -> "Yesterday"
            else -> sdf.format(date).toString()
        }


//    val currentDate = Calendar.getInstance()
//    val currentYear = currentDate[Calendar.YEAR]
//    val currentMonth = currentDate[Calendar.MONTH]
//    val currentDay = currentDate[Calendar.DAY_OF_MONTH]
//
//    val createdDate = Calendar.getInstance()
//    createdDate.time = date
//    val year = createdDate[Calendar.YEAR]
//    val month = createdDate[Calendar.MONTH]
//    val day = createdDate[Calendar.DAY_OF_MONTH]
//
//    return if((currentYear == year) && (currentMonth == month)){
//        when {
//            currentDay == day -> "Today"
//            day == (currentDay - 1) -> "Yesterday"
//            else -> sdf.format(date).toString()
//        }
//    }
//    else
//        sdf.format(date).toString()
}

fun Float.roundOff(): String {
    return String.format("%.2f", this)
}

fun String.titleCase(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}


fun Context.isDarkThemeOn(): Boolean {
    return resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
}

fun getCategoryDrawableResource(category: Int): Int {
    return when (category) {
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
    return when (dateFilter) {
        DateFilter.AFTER.ordinal -> R.drawable.after
        DateFilter.BEFORE.ordinal -> R.drawable.before
        else -> R.drawable.before
    }
}

fun getAmountFilterDrawableResource(amountFilter: Int): Int {
    return when (amountFilter) {
        AmountFilter.BELOW.ordinal -> R.drawable.below
        AmountFilter.ABOVE.ordinal -> R.drawable.above
        else -> R.drawable.above
    }
}

fun getGroupFilterDrawableResource(groupFilter: Int): Int {
    return when (groupFilter) {
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

fun hasImage(view: ImageView): Boolean{
    val drawable = view.drawable
    var hasImage = (drawable != null)

    if(hasImage && (drawable is BitmapDrawable)){
        hasImage = (drawable as BitmapDrawable).bitmap != null
    }
    return hasImage
}

val unwantedWords = listOf<String>(
    "group",
    "family",
    "trip",
    "tour",
    "team",
    "vacation",
    "friend",
    "friends",
    "to",
    "with",
    "plan",
    "meet",
    "the"
)

fun removeIrrelevantWords(groupName: String): String {
    val words = groupName.split(" ")
    var finalString = ""
    for (word in words) {
        if (word.lowercase() !in unwantedWords)
            finalString += "$word "
    }

    return if (finalString != "") finalString
    else "Tour"
}

fun MutableList<Member>?.printableMember(): String {
    var members = ""
    return if (this != null) {
        for (member in this) {
            members += "${member.name}, "
        }
        members.substring(startIndex = 0, endIndex = (members.length - 2))
    } else
        members
}

fun getCategory(ordinal: Int): Category {
    return when (ordinal) {
        Category.FOOD.ordinal -> Category.FOOD
        Category.TRAVEL.ordinal -> Category.TRAVEL
        Category.TICKETS.ordinal -> Category.TICKETS
        Category.RENT.ordinal -> Category.RENT
        Category.REPAIRS.ordinal -> Category.REPAIRS
        Category.ESSENTIALS.ordinal -> Category.ESSENTIALS
        Category.FEES.ordinal -> Category.FEES
        Category.ENTERTAINMENT.ordinal -> Category.ENTERTAINMENT
        Category.OTHERS.ordinal -> Category.OTHERS
        else -> Category.OTHERS
    }
}

fun String.formatNumber(): String {
    var number = ""
    "[^0-9]".toRegex().apply {
        number = replace(this, "")
    }

    return if (number.length == 10) number
    else number.substring(2)

}
fun mergeList(previouslySelected: List<Int>, currentlySelected: List<Int>): List<Int> {
    val newList = mutableListOf<Int>()

    for(i in previouslySelected)
        newList.add(i)

    for(i in currentlySelected) {
        //if(i in newList)
        newList.add(i)
    }

    return newList
}

fun Int.dpToPx(displayMetrics: DisplayMetrics) = (this * displayMetrics.density).toInt()

fun Int.pxToDp(displayMetrics: DisplayMetrics) = (this / displayMetrics.density).toInt()

fun getEmoji(ordinal: Int): String{
    return when (ordinal) {
        Category.FOOD.ordinal -> "🍟"
        Category.TRAVEL.ordinal -> "🚗"
        Category.TICKETS.ordinal -> "🎟"
        Category.RENT.ordinal -> "🔑"
        Category.REPAIRS.ordinal -> "🛠"
        Category.ESSENTIALS.ordinal -> "🛍"
        Category.FEES.ordinal -> "🎫"
        Category.ENTERTAINMENT.ordinal -> "🎬"
        Category.OTHERS.ordinal -> "💰"
        else -> "💰"
    }
}

fun String.getBold(): String{
    //  "\\033[1m$this\\033[0m"
    // "<b>$this</b>"
    // Html.fromHtml("<b>$this</b>").toString()

//    val spannableString = SpannableString(this)
//    val styleSpan = StyleSpan(Typeface.BOLD)
//    val textAppearanceSpan = TextAppearanceSpan(
//        null,
//        Typeface.BOLD,
//        -1,
//        null,
//        null
//    )
//    spannableString.setSpan(
//        textAppearanceSpan, 0, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//    return spannableString.toString()

    "\uD835\uDC2D\uD835\uDC1E\uD835\uDC2C\uD835\uDC2D"
    "\uD835\uDC2D\uD835\uDC1E\uD835\uDC2C\uD835\uDC2D"
    "\uD835\uDE01\uD835\uDDF2\uD835\uDE00\uD835\uDE01"
    return "*$this*"
}

//fun View.ripple(): View {
//    val value = TypedValue()
//    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, value, true)
//    setBackgroundResource(value.resourceId)
//    isFocusable = true // Required for some view types
//    return this
//}

@SuppressLint("RestrictedApi")
fun View.ripple(context: Context){
    val attrs = intArrayOf(android.R.attr.selectableItemBackground)
    val typedArray: TypedArray? = getActivity(context)?.obtainStyledAttributes(attrs)
    val backgroundResource = typedArray?.getResourceId(0, 0)
    if(backgroundResource != null) {
        Log.d(TAG, "ripple: bg res not null res is ${backgroundResource}")
        this.setBackgroundResource(R.color.view_color)

        Handler(Looper.getMainLooper()).postDelayed({
            this.setBackgroundColor(android.R.attr.selectableItemBackground)
        },this.resources.getInteger(R.integer.reply_motion_duration_small).toLong())
    }
    else{
        Log.d(TAG, "ripple: bg res null")
    }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard(){
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

@SuppressLint("RestrictedApi")
fun String.translate(context: Context): String{
    val resource = getActivity(context)?.resources

    return when(this){
        "Date" -> resource?.getString(R.string.date).toString()
        "Above" -> resource?.getString(R.string.above).toString()
        "Below" -> resource?.getString(R.string.below).toString()
        "After" -> resource?.getString(R.string.after).toString()
        "Before" -> resource?.getString(R.string.before).toString()
        "Amount" -> resource?.getString(R.string.amount).toString()
        "Food" -> resource?.getString(R.string.food).toString()
        "Travel" -> resource?.getString(R.string.travel).toString()
        "Tickets" -> resource?.getString(R.string.tickets).toString()
        "Rent" -> resource?.getString(R.string.rent).toString()
        "Fees" -> resource?.getString(R.string.fees).toString()
        "Repairs" -> resource?.getString(R.string.repairs).toString()
        "Entertainment" -> resource?.getString(R.string.entertainment).toString()
        "Essentials" -> resource?.getString(R.string.essentials).toString()
        "Others" -> resource?.getString(R.string.others).toString()
        else -> resource?.getString(R.string.others).toString()
    }
}

fun recipients() = arrayOf("keerthik97117@gmail.com")

fun getGroupIds(groups: MutableList<Group>): List<Int> {
    val groupIds = mutableListOf<Int>()
    for (group in groups) {
        groupIds.add(group.groupId)
    }
    return groupIds.toList()
}

fun getMemberIds(members: List<Member>): MutableList<Int> {
    val memberIds = mutableListOf<Int>()
    for (member in members) {
        memberIds.add(member.memberId)
    }
    return memberIds
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}



fun decodeSampledBitmapFromUri(
    context: Context,
    uri: Uri?,
    reqWidth: Int,
    reqHeight: Int
): Bitmap? {
    // First decode with inJustDecodeBounds=true to check dimensions
    val bitmap = uri?.let {
        BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, this)
        }
    }
    bitmap?.let {
        Log.d(
            TAG,
            "decodeSampledBitmapFromUri: bytecount ${it.byteCount} density ${it.density} height ${it.height} width ${it.width}")
    }

//    val bm = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
//
//    bm?.let {
//        Log.d(
//            TAG,
//            "decodeSampledBitmapFromUri: bytecount ${it.byteCount} density ${it.density} height ${it.height} width ${it.width}")
//    }

    return bitmap
}



fun BitmapFactory.decodeUri(context: Context, uri: Uri): Bitmap?{
    return try{
        BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
    }
    catch (e: Exception){
        null
    }
}

val dummyMember = Member(
    "",
    1,
    null
)

val dummyMemberExpense = ExpenseMember(
    -1,
    -1,
    "test",
    1,
    1f,
    1f,
    1,
    dummyMember,
    Date()
)

val dummyGroup = Group(
    "",
    "",
    Date(),
    Date(),
    1F,
    null,
).apply {
    groupId = -2 // for empty group view
}

fun getRoundedCroppedBitmap(bitmap: Bitmap): Bitmap? {
    val widthLight = bitmap.width
    val heightLight = bitmap.height
    val minVal = widthLight.coerceAtMost(heightLight)
    val output = Bitmap.createBitmap(
        //bitmap.width, bitmap.height,
        minVal, minVal,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(output)
    val paintColor = Paint()
    paintColor.flags = Paint.ANTI_ALIAS_FLAG

    //val maxVal = widthLight.coerceAtMost(heightLight)
    val rectF = RectF(Rect(0, 0, minVal, minVal))

    canvas.drawRoundRect(rectF, (minVal / 2).toFloat(), (minVal/2).toFloat(), paintColor)
    //canvas.drawRoundRect(rectF, 60f, 60f, paintColor)
    Log.d(TAG, "getRoundedCroppedBitmap: ${(widthLight / 2).toFloat()} ${(heightLight/2).toFloat()} ${minVal/2}")
    val paintImage = Paint()
    paintImage.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
    canvas.drawBitmap(bitmap, 0f, 0f, paintImage)
    return output
}

fun vibrate(context: Context, delete: Boolean){

    val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
// Vibrate for 500 milliseconds
// Vibrate for 500 milliseconds
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // for delete single vibration
        if(delete)
        v!!.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        else
        v!!.vibrate(VibrationEffect.createWaveform(longArrayOf(1, 2, 30, 50, 80), -1))
        // for no action double vibration
    } else {
        //deprecated in API 26
        v!!.vibrate(50)
    }

}

fun playDeleteSound(context: Context){
    val mediaPlayer = MediaPlayer.create(context, R.raw.swipe)
    mediaPlayer.start()
}

fun playPaymentSuccessSound(context: Context){
    val mediaPlayer = MediaPlayer.create(context, R.raw.payment_success)
    mediaPlayer.start()
}

fun changeStatusBar(activity: Activity){
    val window = activity.window
    window.apply {
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        statusBarColor = activity.resources.getColor(R.color.green)
    }

    Handler(Looper.getMainLooper()).postDelayed(
        {
            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                statusBarColor = activity.resources.getColor(R.color.status_bar_color)
            }
        }, 400
    )

}


///**
// * Check whether [effectId] is supported by the device's Vibrator.
// */
//private fun isPrimitiveSupported(effectId: Int): Boolean {
//    return vibratorManager.defaultVibrator.areAllPrimitivesSupported(effectId)
//}
//
///**
// * Try making vibration with the given [effectId] if the device supports it. Otherwise,
// * show error message.
// */
//private fun tryVibrate(vibratorManager: VibratorManager, effectId: Int, context: Context) {
//    if (isPrimitiveSupported(effectId)) {
//        vibratorManager.vibrate(
//            CombinedVibration.createParallel(
//                VibrationEffect.startComposition()
//                    .addPrimitive(effectId)
//                    .compose()
//            )
//        )
//    } else {
//        Toast.makeText(
//            context,
//            "This primitive is not supported by this device.",
//            Toast.LENGTH_LONG,
//        ).show()
//    }
//}
