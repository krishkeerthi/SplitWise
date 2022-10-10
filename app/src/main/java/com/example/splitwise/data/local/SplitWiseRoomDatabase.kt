package com.example.splitwise.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.splitwise.data.local.dao.*
import com.example.splitwise.data.local.entity.*
import com.example.splitwise.util.*

@Database(
    entities = [ ExpenseBill::class, ExpensePayee::class, Expense::class, GroupExpense::class, GroupMember::class,
        Group::class, Member::class, Transaction::class, MemberStreak::class, RemovedExpensePayee::class] ,
    version = 1
)
@TypeConverters(
    DateConverter::class,
    UriConverter::class
)
abstract class SplitWiseRoomDatabase: RoomDatabase() {
    abstract fun expenseBillDao():ExpenseBillDao
    abstract fun expensePayeeDao(): ExpensePayeeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun groupExpenseDao(): GroupExpenseDao
    abstract fun groupMemberDao(): GroupMemberDao
    abstract fun groupDao(): GroupDao
    abstract fun transactionDao(): TransactionDao
    abstract fun memberDao(): MemberDao
    abstract fun memberStreakDao(): MemberStreakDao
    abstract fun removedExpensePayeeDao(): RemovedExpensePayeeDao

    companion object{
        @Volatile
        private var instance: SplitWiseRoomDatabase? = null

        fun getInstance(context: Context): SplitWiseRoomDatabase{
            return instance ?: synchronized(this){
                instance ?: buildDatabaseInstance(context).also{
                    instance = it
                }
            }
        }

        private fun buildDatabaseInstance(context: Context): SplitWiseRoomDatabase{
            return Room.databaseBuilder(
                context.applicationContext,
                SplitWiseRoomDatabase::class.java,
                "split_wise_database"
            ).build()
//                .createFromAsset("database/splitwise_dummy.db")
//                .build()
        }
    }
}