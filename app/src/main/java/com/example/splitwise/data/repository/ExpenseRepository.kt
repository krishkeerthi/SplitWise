package com.example.splitwise.data.repository

import android.net.Uri
import com.example.splitwise.data.datasource.ExpenseDataSource
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Expense
import com.example.splitwise.data.local.localdatasource.ExpenseLocalDataSource
import com.example.splitwise.model.BillUri
import com.example.splitwise.util.titleCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class ExpenseRepository(
    database: SplitWiseRoomDatabase
) {

    private var dataSource: ExpenseDataSource = ExpenseLocalDataSource(
        database.expenseDao(),
        database.expensePayeeDao(),
        database.expenseBillDao(),
        database.removedExpensePayeeDao(),
        database.groupExpenseDao()
    )

    suspend fun createExpense(
        groupId: Int, name: String, category: Int, totalAmount: Float,
        splitAmount: Float, payer: Int, date: Date
    ): Int {
        return withContext(Dispatchers.IO) {
            dataSource.createExpense(
                groupId,
                name.titleCase(),
                category,
                totalAmount,
                splitAmount,
                payer,
                date
            )
        }
    }

    suspend fun addExpensePayee(expenseId: Int, payeeId: Int) {
        dataSource.addExpensePayee(expenseId, payeeId)
    }

    suspend fun getExpensePayees(expenseId: Int): List<Int>? {
        return withContext(Dispatchers.IO) { dataSource.getExpensePayees(expenseId) }
    }

    suspend fun addExpenseBill(expenseId: Int, uri: Uri) {
        dataSource.addExpenseBill(expenseId, uri)
    }

    suspend fun getExpenseBills(expenseId: Int): List<Uri>? {
        return withContext(Dispatchers.IO) { dataSource.getExpenseBills(expenseId) }
    }

    suspend fun getExpenseBillsWithId(expenseId: Int): List<BillUri>? {
        return withContext(Dispatchers.IO) { dataSource.getExpenseBillsWithId(expenseId) }
    }

    suspend fun getExpense(expenseId: Int): Expense? {
        return withContext(Dispatchers.IO) { dataSource.getExpense(expenseId) }
    }

    suspend fun getExpenses(groupId: Int): List<Expense>? {
        return withContext(Dispatchers.IO) { dataSource.getExpenses(groupId) }
    }

    suspend fun getExpensesByCategory(groupId: Int, category: Int): List<Expense>? {
        return withContext(Dispatchers.IO) { dataSource.getExpensesByCategory(groupId, category) }
    }

    suspend fun getExpensesByCategories(groupId: Int, categories: List<Int>): List<Expense>? {
        return withContext(Dispatchers.IO) {
            dataSource.getExpensesByCategories(
                groupId,
                categories
            )
        }
    }

    // Need to implement
    suspend fun getExpensesWithConstraint(groupId: Int): List<Expense>? {
        return dataSource.getExpenses(groupId)
    }

    suspend fun deleteBill(billId: Int) {
        dataSource.deleteExpenseBill(billId)
    }

    suspend fun removeExpensePayee(expenseId: Int, payeeId: Int) {
        withContext(Dispatchers.IO) {
            dataSource.removeExpensePayee(expenseId, payeeId)
        }
    }

    suspend fun addPayeeToRemovedExpensePayees(expenseId: Int, payeeId: Int) {
        withContext(Dispatchers.IO) {
            dataSource.addRemovedExpensePayee(expenseId, payeeId)
        }
    }

    suspend fun deleteBills(expenseId: Int) {
        withContext(Dispatchers.IO) {
            dataSource.removeBills(expenseId)
        }
    }

    suspend fun removeExpenseIdFromGroup(groupId: Int, expenseId: Int) {
        withContext(Dispatchers.IO){
            dataSource.removeExpenseIdFromGroup(groupId, expenseId)
        }
    }

    suspend fun deleteExpense(expenseId: Int) {
        withContext(Dispatchers.IO){
            dataSource.deleteExpense(expenseId)
        }
    }

    suspend fun getRemovedExpensePayees(expenseId: Int): List<Int>? {
        return withContext(Dispatchers.IO){
            dataSource.getRemovedExpensePayees(expenseId)
        }
    }

}