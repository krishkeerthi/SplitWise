package com.example.splitwise.data.local.localdatasource

import android.net.Uri
import com.example.splitwise.data.datasource.ExpenseDataSource
import com.example.splitwise.data.local.dao.ExpenseBillDao
import com.example.splitwise.data.local.dao.ExpenseDao
import com.example.splitwise.data.local.dao.ExpensePayeeDao
import com.example.splitwise.data.local.entity.Expense
import com.example.splitwise.data.local.entity.ExpenseBill
import com.example.splitwise.data.local.entity.ExpensePayee
import java.net.URI
import java.util.*


//repository class, which will serve as a single source of truth for the app's data,
// and abstract the source of the data (network, cache, etc.) out of the view model.
class ExpenseLocalDataSource(
    private val expenseDao: ExpenseDao,
    private val expensePayeeDao: ExpensePayeeDao,
    private val expenseBillDao: ExpenseBillDao
): ExpenseDataSource {
    override suspend fun createExpense(
        groupId: Int,
        name: String,
        category: Int,
        totalAmount: Float,
        splitAmount: Float,
        payer: Int,
        date: Date
    ): Int {
        return expenseDao.insert(
            Expense(
                groupId,
                name,
                category,
                totalAmount,
                splitAmount,
                payer,
                date
            )
        ).toInt()
    }

    override suspend fun addExpensePayee(expenseId: Int, payeeId: Int) {
        expensePayeeDao.insert(
            ExpensePayee(expenseId, payeeId)
        )
    }

    override suspend fun getExpensePayees(expenseId: Int): List<Int>? {
        return expensePayeeDao.getPayees(expenseId)
    }

    override suspend fun addExpenseBill(expenseId: Int, uri: Uri) {
        expenseBillDao.insert(
            ExpenseBill(expenseId, uri)
        )
    }

    override suspend fun getExpenseBills(expenseId: Int): List<Uri>? {
        return expenseBillDao.getBills(expenseId)
    }

    override suspend fun getExpenses(groupId: Int): List<Expense>? {
        return expenseDao.getAllExpenses(groupId)
    }

    override suspend fun getExpense(expenseId: Int): Expense? {
        return expenseDao.getExpense(expenseId)
    }

    override suspend fun getExpensesByCategory(groupId: Int, category: Int): List<Expense>? {
        return expenseDao.getExpensesByCategory(groupId, category)
    }

    override suspend fun getExpensesByCategories(
        groupId: Int,
        categories: List<Int>
    ): List<Expense>? {
        return expenseDao.getExpensesByCategories(groupId, categories)
    }
}