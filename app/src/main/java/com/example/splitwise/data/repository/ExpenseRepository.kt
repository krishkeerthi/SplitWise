package com.example.splitwise.data.repository

import android.content.Context
import android.net.Uri
import com.example.splitwise.data.datasource.ExpenseDataSource
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Expense
import com.example.splitwise.data.local.localdatasource.ExpenseLocalDataSource
import com.example.splitwise.data.local.localdatasource.GroupLocalDataSource
import com.example.splitwise.util.titleCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.exp

class ExpenseRepository(
    private val database: SplitWiseRoomDatabase
) {

    private var dataSource: ExpenseDataSource = ExpenseLocalDataSource(
        database.expenseDao(),
        database.expensePayeeDao(),
        database.expenseBillDao()
    )

    suspend fun createExpense(groupId: Int, name:String, category: Int, totalAmount: Float,
                              splitAmount: Float, payer: Int, date: Date
    ): Int{
        return withContext(Dispatchers.IO){dataSource.createExpense(groupId, name.titleCase(), category, totalAmount, splitAmount, payer, date)}
    }

    suspend fun addExpensePayee(expenseId: Int, payeeId: Int){
        dataSource.addExpensePayee(expenseId, payeeId)
    }

    suspend fun getExpensePayees(expenseId: Int): List<Int>?{
        return withContext(Dispatchers.IO){dataSource.getExpensePayees(expenseId)}
    }

    suspend fun addExpenseBill(expenseId: Int, uri: Uri){
        dataSource.addExpenseBill(expenseId, uri)
    }

    suspend fun getExpenseBills(expenseId: Int): List<Uri>?{
        return withContext(Dispatchers.IO){dataSource.getExpenseBills(expenseId)}
    }

    suspend fun getExpense(expenseId: Int): Expense?{
        return withContext(Dispatchers.IO){dataSource.getExpense(expenseId)}
    }

    suspend fun getExpenses(groupId: Int): List<Expense>?{
        return withContext(Dispatchers.IO){dataSource.getExpenses(groupId)}
    }

    // Need to implement
    suspend fun getExpensesWithConstraint(groupId: Int): List<Expense>?{
        return dataSource.getExpenses(groupId)
    }
}