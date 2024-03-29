package com.example.splitwise.ui.fragment.settings

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.data.repository.TransactionRepository
import com.example.splitwise.ui.SplitwiseApplication
import com.example.splitwise.util.*
import kotlinx.coroutines.launch
import java.util.*

class SettingsViewModel(val context: Context) :
    ViewModel() {
    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val transactionRepository = TransactionRepository(database)
    private val expenseRepository = ExpenseRepository(database)
    private val memberRepository = MemberRepository(database)
    private val groupRepository = GroupRepository(database)

    private val REQUEST_CODE = 0

    private val notifyIntent =
        Intent(SplitwiseApplication.getApplication(), AlarmReceiver::class.java)

    private val notifyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
        SplitwiseApplication.getApplication(),
        REQUEST_CODE,
        notifyIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

    fun insertSampleData(updateDateInserted: () -> Unit, restartActivity: () -> Unit) {
        viewModelScope.launch {
            // Group Creation
            // Group 1
            val groupId = groupRepository.createGroup(
                "Family trip to shirdi",
                "description",
                Date(),
                0F,
                Uri.parse("android.resource://com.example.splitwise/drawable/shirdi"),
            )

            // Member Addition to Group
            //Uri.parse("android.resource://com.example.splitwise/drawable/kamal"),
            val shanthiId = memberRepository.addMember(
                "Shanthi", 3839393939,
                Uri.parse("android.resource://com.example.splitwise/drawable/shanthi"),
            )
            memberRepository.addMemberStreak(shanthiId)
            groupRepository.addGroupMember(groupId, shanthiId)


            val arjunId = memberRepository.addMember(
                "Arjun", 2930492043,
                Uri.parse("android.resource://com.example.splitwise/drawable/arjun"),
            )
            memberRepository.addMemberStreak(arjunId)
            groupRepository.addGroupMember(groupId, arjunId)

            val kamalId = memberRepository.addMember(
                "Kamal", 2493020303,
                Uri.parse("android.resource://com.example.splitwise/drawable/kamal"),
            )
            memberRepository.addMemberStreak(kamalId)
            groupRepository.addGroupMember(groupId, kamalId)

            // Expense Addition
            // Expense 1
            val expenseId = expenseRepository.createExpense(
                groupId,
                "Shirdi Restaurant",
                0,
                999f,
                333f,
                shanthiId,
                Date()
            )

            expenseRepository.addExpensePayee(expenseId, shanthiId)
            memberRepository.incrementStreak(shanthiId)

            expenseRepository.addExpensePayee(expenseId, arjunId)
            memberRepository.incrementStreak(arjunId)
            transactionRepository.addTransaction(groupId, shanthiId, arjunId, 333f)

            expenseRepository.addExpensePayee(expenseId, kamalId)
            memberRepository.incrementStreak(kamalId)
            transactionRepository.addTransaction(groupId, shanthiId, kamalId, 333f)

            groupRepository.addGroupExpense(groupId, expenseId)

            groupRepository.updateTotalExpense(groupId, 999f)
            // Expense 2
            val expenseId2 = expenseRepository.createExpense(
                groupId,
                "Temple Tickets",
                2,
                666f,
                222f,
                kamalId,
                Date()
            )

            expenseRepository.addExpensePayee(expenseId2, kamalId)
            memberRepository.incrementStreak(kamalId)

            expenseRepository.addExpensePayee(expenseId2, shanthiId)
            memberRepository.incrementStreak(shanthiId)
            transactionRepository.addTransaction(groupId, kamalId, shanthiId, 222f)

            expenseRepository.addExpensePayee(expenseId2, arjunId)
            memberRepository.incrementStreak(arjunId)
            transactionRepository.addTransaction(groupId, kamalId, arjunId, 222f)

            groupRepository.addGroupExpense(groupId, expenseId2)

            groupRepository.updateTotalExpense(groupId, 666f)


            // Group 2
            val groupId2 = groupRepository.createGroup(
                "Goa tour with friends",
                "description",
                Date(),
                0F,
                Uri.parse("android.resource://com.example.splitwise/drawable/goa_sample"),
            )

            // Member Addition to Group
            val reshmiId = memberRepository.addMember(
                "Reshmi", 3839393939,
                Uri.parse("android.resource://com.example.splitwise/drawable/reshmi"),
            )
            memberRepository.addMemberStreak(reshmiId)
            groupRepository.addGroupMember(groupId2, reshmiId)


            val krithiId = memberRepository.addMember(
                "Krithi", 2930492043,
                Uri.parse("android.resource://com.example.splitwise/drawable/krithi"),
            )
            memberRepository.addMemberStreak(krithiId)
            groupRepository.addGroupMember(groupId2, krithiId)

            val nazriyaId = memberRepository.addMember(
                "Nazriya", 2493020303,
                Uri.parse("android.resource://com.example.splitwise/drawable/nazriya"),
            )
            memberRepository.addMemberStreak(nazriyaId)
            groupRepository.addGroupMember(groupId2, nazriyaId)

            // Expense Addition
            // Expense 1
            val expenseId21 = expenseRepository.createExpense(
                groupId2,
                "Scuba diving",
                2,
                3333f,
                1111f,
                nazriyaId,
                Date()
            )

            expenseRepository.addExpensePayee(expenseId21, nazriyaId)
            memberRepository.incrementStreak(nazriyaId)

            expenseRepository.addExpensePayee(expenseId21, reshmiId)
            memberRepository.incrementStreak(reshmiId)
            transactionRepository.addTransaction(groupId2, nazriyaId, reshmiId, 1111f)

            expenseRepository.addExpensePayee(expenseId21, krithiId)
            memberRepository.incrementStreak(krithiId)
            transactionRepository.addTransaction(groupId2, nazriyaId, krithiId, 1111f)

            groupRepository.addGroupExpense(groupId2, expenseId21)

            groupRepository.updateTotalExpense(groupId2, 3333f)
            // Expense 2
            val expenseId22 = expenseRepository.createExpense(
                groupId2,
                "Bungee jump",
                2,
                6666f,
                2222f,
                reshmiId,
                Date()
            )

            expenseRepository.addExpensePayee(expenseId22, reshmiId)
            memberRepository.incrementStreak(reshmiId)

            expenseRepository.addExpensePayee(expenseId22, nazriyaId)
            memberRepository.incrementStreak(nazriyaId)
            transactionRepository.addTransaction(groupId2, reshmiId, nazriyaId, 2222f)

            expenseRepository.addExpensePayee(expenseId22, krithiId)
            memberRepository.incrementStreak(krithiId)
            transactionRepository.addTransaction(groupId2, reshmiId, krithiId, 2222f)

            groupRepository.addGroupExpense(groupId2, expenseId22)

            groupRepository.updateTotalExpense(groupId2, 6666f)

            // callback
            updateDateInserted()
            restartActivity()

        }

        //createNotificationChannel(context) // context leaks

       // startNotifications()
    }

    private fun startNotifications() {
        val notificationManager =
            ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager

        notificationManager.cancelNotifications()

        //val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
//        val alarmManager = SplitwiseApplication.getApplication().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        //if(alarmManager != null)
//        Log.d(TAG, "startNotifications: alarm alarm manager ${alarmManager}")

//        alarmManager.setRepeating(
//            AlarmManager.RTC_WAKEUP,
//            Calendar.getInstance().timeInMillis,
//            1000*60,
//            notifyPendingIntent
//        )

//        val cal = Calendar.getInstance()
//
//        cal[Calendar.HOUR_OF_DAY] = 16
//        cal[Calendar.MINUTE] = 30
//        cal[Calendar.SECOND] = 0
//        alarmManager.setExact(
//            AlarmManager.RTC_WAKEUP,
//            cal.timeInMillis,
//            notifyPendingIntent
//        )

//        val notificationManager = ContextCompat.getSystemService(
//            context,
//            NotificationManager::class.java
//        ) as NotificationManager
//
//        notificationManager.sendNotification(context)

//        val intent = Intent(context, NotificationWorker::class.java).apply {
//        }
//        context.startActivity(intent)


//        val notificationManager = ContextCompat.getSystemService(
//            context,
//            NotificationManager::class.java
//        ) as NotificationManager

        notificationManager.sendNotification(context)
    }
}

class SettingsViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(context) as T
    }
}