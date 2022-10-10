package com.example.splitwise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.splitwise.data.local.entity.Expense
import java.util.*

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expense WHERE group_id = :groupId")
    suspend fun getAllExpenses(groupId: Int): List<Expense>?

    @Query("SELECT * FROM expense WHERE expense_id = :expenseId")
    suspend fun getExpense(expenseId: Int): Expense?

    @Query("SELECT * FROM expense WHERE group_id = :groupId AND expense_name LIKE :name || '%'")   // or append % at the end of name parameter. i.e name = "munar%"
    suspend fun getExpensesStartsWith(groupId: Int, name: String): List<Expense>?

    @Query("SELECT * FROM expense WHERE group_id = :groupId AND category = :category")
    suspend fun getExpensesByCategory(groupId: Int, category: Int): List<Expense>?

    @Query("SELECT * FROM expense WHERE group_id = :groupId AND payer = :payerId")
    suspend fun getExpensesByPayer(groupId: Int, payerId: Int): List<Expense>?

    @Query("SELECT * FROM expense WHERE group_id = :groupId AND date = :date")
    suspend fun getExpensesOnDate(groupId: Int, date: Date): List<Expense>?

    @Query("SELECT * FROM expense WHERE group_id = :groupId AND date < :date")
    suspend fun getExpensesBeforeDate(groupId: Int, date: Date): List<Expense>?

    @Query("SELECT * FROM expense WHERE group_id = :groupId AND date >= :date")
    suspend fun getExpensesAfterDate(groupId: Int, date: Date): List<Expense>?

    @Query("SELECT * FROM expense WHERE group_id = :groupId AND total_amount < :amount")
    suspend fun getExpensesBelow(groupId: Int, amount: Float): List<Expense>?

    @Query("SELECT * FROM expense WHERE group_id = :groupId AND total_amount >= :amount")
    suspend fun getExpensesAbove(groupId: Int, amount: Float): List<Expense>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Query("SELECT * FROM expense WHERE group_id = :groupId AND category IN (:categories) ")
    suspend fun getExpensesByCategories(groupId: Int, categories: List<Int>): List<Expense>?

    @Query("DELETE FROM expense WHERE expense_id = :expenseId")
    suspend fun deleteExpense(expenseId: Int)
}