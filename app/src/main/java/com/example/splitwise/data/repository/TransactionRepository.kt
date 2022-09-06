package com.example.splitwise.data.repository

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
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

    suspend fun addTransaction(groupId: Int, payerId: Int, payeeId: Int, amount: Float){
        dataSource.addTransaction(groupId, payerId, payeeId, amount)
    }
    suspend fun settle(senderId: Int, receiverId: Int){
        dataSource.settle(senderId, receiverId)
    }

    suspend fun settle(senderId: Int, receiverId: Int, groupId: Int){
        dataSource.settle(senderId, receiverId, groupId)
    }

    suspend fun settle(senderId: Int, receiverIds: List<Int>, groupIds: List<Int>){
        Log.d(TAG, "settle: selected repository ${receiverIds}")
        dataSource.settle(senderId, receiverIds, groupIds)
    }

    suspend fun settleAllInGroup(senderId: Int, groupId: Int){
        dataSource.settleAllInGroup(senderId, groupId)
    }

    suspend fun settleAll(senderId: Int){
        dataSource.settleAll(senderId)
    }

    suspend fun getPayers(payeeId: Int): List<Int>?{
        return withContext(Dispatchers.IO){
            val payerIds = dataSource.getPayers(payeeId)
            payerIds?.toSet()?.toList()
        }
    }

    suspend fun getPayers(payeeId: Int, groupId: Int): List<Int>?{
        return withContext(Dispatchers.IO){
            val payerIds = dataSource.getPayers(payeeId, groupId)
            payerIds?.toSet()?.toList()
        }
    }

    suspend fun getPayers(payeeId: Int, groupIds: List<Int>): List<Int>?{
        return withContext(Dispatchers.IO){
            val payerIds = dataSource.getPayers(payeeId, groupIds)
            payerIds?.toSet()?.toList()
        }
    }

    suspend fun transactionStats(): List<MemberPaymentStats>?{
        return withContext(Dispatchers.IO){dataSource.transactionStats()}
    }

    suspend fun transactionStats(groupId: Int): List<MemberPaymentStats>?{
        return withContext(Dispatchers.IO){dataSource.transactionStats(groupId)}
    }

    suspend fun transactionStats(groupIds: List<Int>): List<MemberPaymentStats>?{
        return withContext(Dispatchers.IO){dataSource.transactionStats(groupIds)}
    }

    suspend fun getOwed(senderId: Int, receiverId: Int): Float?{
        return withContext(Dispatchers.IO){dataSource.getOwed(senderId, receiverId)}
    }

    suspend fun getOwed(senderId: Int, receiverIds: List<Int>, groupIds: List<Int>): Float?{
        return withContext(Dispatchers.IO){dataSource.getOwed(senderId, receiverIds, groupIds)}
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