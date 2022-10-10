package com.example.splitwise.data.local.localdatasource

import android.net.Uri
import com.example.splitwise.data.datasource.ExpenseDataSource
import com.example.splitwise.data.local.dao.*
import com.example.splitwise.data.local.entity.*
import com.example.splitwise.model.BillUri
import java.net.URI
import java.util.*
import kotlin.math.exp


//repository class, which will serve as a single source of truth for the app's data,
// and abstract the source of the data (network, cache, etc.) out of the view model.
class ExpenseLocalDataSource(
    private val expenseDao: ExpenseDao,
    private val expensePayeeDao: ExpensePayeeDao,
    private val expenseBillDao: ExpenseBillDao,
    private val removedExpensePayeeDao: RemovedExpensePayeeDao,
    private val groupExpenseDao: GroupExpenseDao
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

    override suspend fun getExpenseBillsWithId(expenseId: Int): List<BillUri>? {
        return expenseBillDao.getBillsWithId(expenseId)
    }

    override suspend fun getExpenses(groupId: Int): List<Expense>? {
        return expenseDao.getAllExpenses(groupId)
    }

    override suspend fun getExpense(expenseId: Int): Expense? {
        return expenseDao.getExpense(expenseId)
    }

    override suspend fun deleteExpenseBill(billId: Int) {
        expenseBillDao.deleteBill(billId)
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

    override suspend fun removeExpensePayee(expenseId: Int, payeeId: Int) {
        expensePayeeDao.removePayee(expenseId, payeeId)
    }

    override suspend fun addRemovedExpensePayee(expenseId: Int, payeeId: Int) {
        removedExpensePayeeDao.insert(RemovedExpensePayee(expenseId, payeeId))
    }

    override suspend fun removeBills(expenseId: Int) {
        expenseBillDao.deleteBills(expenseId)
    }

    override suspend fun removeExpenseIdFromGroup(groupId: Int, expenseId: Int) {
        groupExpenseDao.removeExpense(groupId, expenseId)
    }

    override suspend fun deleteExpense(expenseId: Int) {
        expenseDao.deleteExpense(expenseId)
    }

    override suspend fun getRemovedExpensePayees(expenseId: Int): List<Int>? {
        return removedExpensePayeeDao.getPayees(expenseId)
    }


}