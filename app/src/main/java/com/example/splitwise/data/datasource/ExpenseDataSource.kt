package com.example.splitwise.data.datasource

import android.net.Uri
import com.example.splitwise.data.local.entity.Expense
import com.example.splitwise.data.local.entity.MemberStreak
import com.example.splitwise.model.BillUri
import java.net.URI
import java.util.*

interface ExpenseDataSource {

    suspend fun createExpense(groupId: Int, name:String, category: Int, totalAmount: Float,
                              splitAmount: Float, payer: Int, date: Date): Int

    suspend fun addExpensePayee(expenseId: Int, payeeId: Int)

    suspend fun getExpensePayees(expenseId: Int): List<Int>?

    suspend fun addExpenseBill(expenseId: Int, uri: Uri)

    suspend fun getExpenseBills(expenseId: Int): List<Uri>?

    suspend fun getExpenseBillsWithId(expenseId: Int): List<BillUri>?

    suspend fun getExpenses(groupId: Int): List<Expense>?

    suspend fun getExpense(expenseId: Int): Expense?

    suspend fun deleteExpenseBill(billId: Int)

    suspend fun getExpensesByCategory(groupId: Int, category: Int): List<Expense>?

    suspend fun getExpensesByCategories(groupId: Int, categories: List<Int>): List<Expense>?

    suspend fun removeExpensePayee(expenseId: Int, payeeId: Int)

    suspend fun addRemovedExpensePayee(expenseId: Int, payeeId: Int)

    suspend fun removeBills(expenseId: Int)

    suspend fun removeExpenseIdFromGroup(groupId: Int, expenseId: Int)

    suspend fun deleteExpense(expenseId: Int)

    suspend fun getRemovedExpensePayees(expenseId: Int): List<Int>?

}