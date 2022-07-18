package com.example.splitwise.data.datasource

import android.net.Uri
import com.example.splitwise.data.local.entity.Expense
import java.net.URI
import java.util.*

interface ExpenseDataSource {

    suspend fun createExpense(groupId: Int, name:String, category: Int, totalAmount: Float,
                              splitAmount: Float, payer: Int, date: Date): Int

    suspend fun addExpensePayee(expenseId: Int, payeeId: Int)

    suspend fun getExpensePayees(expenseId: Int): List<Int>?

    suspend fun addExpenseBill(expenseId: Int, uri: Uri)

    suspend fun getExpenseBills(expenseId: Int): List<Uri>?

    suspend fun getExpenses(groupId: Int): List<Expense>?

    suspend fun getExpense(expenseId: Int): Expense?

    suspend fun getExpensesWithConstraint(groupId: Int): List<Expense>?
}