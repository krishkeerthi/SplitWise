package com.example.splitwise.data.repository

import android.content.Context
import com.example.splitwise.data.datasource.ExpenseDataSource
import com.example.splitwise.data.datasource.TransactionDataSource
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.localdatasource.ExpenseLocalDataSource
import com.example.splitwise.data.local.localdatasource.TransactionLocalDataSource
import com.example.splitwise.model.MemberPaymentStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TransactionRepository(
    private val database: SplitWiseRoomDatabase
) {
    private var dataSource: TransactionDataSource = TransactionLocalDataSource(
        database.transactionDao()
    )

    suspend fun settle(senderId: Int, receiverId: Int){
        dataSource.settle(senderId, receiverId)
    }

    suspend fun settle(senderId: Int, receiverId: Int, groupId: Int){
        dataSource.settle(senderId, receiverId, groupId)
    }

    suspend fun settleAllInGroup(senderId: Int, groupId: Int){
        dataSource.settleAllInGroup(senderId, groupId)
    }

    suspend fun settleAll(senderId: Int){
        dataSource.settleAll(senderId)
    }

    suspend fun getPayers(payeeId: Int): List<Int>?{
        return withContext(Dispatchers.IO){dataSource.getPayers(payeeId)}
    }

    suspend fun getPayers(payeeId: Int, groupId: Int): List<Int>?{
        return withContext(Dispatchers.IO){dataSource.getPayers(payeeId, groupId)}
    }

    suspend fun transactionStats(): List<MemberPaymentStats>?{
        return withContext(Dispatchers.IO){dataSource.transactionStats()}
    }

    suspend fun transactionStats(groupId: Int): List<MemberPaymentStats>?{
        return withContext(Dispatchers.IO){dataSource.transactionStats(groupId)}
    }

    suspend fun getOwed(senderId: Int, receiverId: Int): Float?{
        return withContext(Dispatchers.IO){dataSource.getOwed(senderId, receiverId)}
    }

    suspend fun getOwedInGroup(senderId: Int, receiverId: Int, groupId: Int): Float?{
        return withContext(Dispatchers.IO){dataSource.getOwedInGroup(senderId, receiverId, groupId)}
    }

    suspend fun getOwed(senderId: Int): Float?{
        return withContext(Dispatchers.IO){dataSource.getOwed(senderId)}
    }

    suspend fun getOwedInGroup(senderId: Int, groupId: Int): Float?{
        return withContext(Dispatchers.IO){dataSource.getOwedInGroup(senderId, groupId)}
    }
}