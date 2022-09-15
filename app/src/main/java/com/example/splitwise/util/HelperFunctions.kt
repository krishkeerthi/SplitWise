package com.example.splitwise.util

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.fonts.Font
import android.text.Html
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.Member
import com.google.android.material.internal.ContextUtils
import com.google.android.material.internal.ContextUtils.getActivity
import org.w3c.dom.Text
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

fun nameCheck(value: String): Boolean {
    return value.matches("[a-zA-Z0-9-&\"'.\n /,]+$".toRegex())
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

    for(i in currentlySelected)
        newList.add(i)

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
    if(backgroundResource != null)
        this.setBackgroundResource(backgroundResource)
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
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