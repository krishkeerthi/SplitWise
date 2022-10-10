package com.example.splitwise.data.local.localdatasource

import android.content.ContentValues.TAG
import android.util.Log
import com.example.splitwise.data.datasource.TransactionDataSource
import com.example.splitwise.data.local.dao.TransactionDao
import com.example.splitwise.data.local.entity.Transaction
import com.example.splitwise.model.MemberAmount
import com.example.splitwise.model.MemberPaymentStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TransactionLocalDataSource(
    private val transactionDao: TransactionDao
): TransactionDataSource {
    override suspend fun addTransaction(groupId: Int, payerId: Int, payeeId: Int, amount: Float) {
        transactionDao.insert(Transaction(groupId, payerId, payeeId, amount))
    }

    override suspend fun updateTransaction(
        groupId: Int,
        payerId: Int,
        payeeId: Int,
        amount: Float
    ) {
        val totalAmount = transactionDao.getAmount(groupId, payerId, payeeId)
        if(totalAmount == null){ // no such transaction exists so create new
            transactionDao.insert(Transaction(groupId, payerId, payeeId, amount))
            Log.d(TAG, "updateTransaction: new record inserted")
        }
        else{ // transaction exists between this payer and payee so update
            transactionDao.updateAmount(groupId, payerId, payeeId, totalAmount + amount)
            Log.d(TAG, "updateTransaction: old record updated")
        }
    }

    override suspend fun settle(senderId: Int, receiverId: Int) {
        transactionDao.reduceAmount(senderId, receiverId) // deletes record
    }

    override suspend fun settle(senderId: Int, receiverId: Int, groupId: Int) {
        transactionDao.reduceAmount(senderId, receiverId, groupId) // deletes record
    }

    override suspend fun settle(senderId: Int, receiverIds: List<Int>, groupIds: List<Int>) { //
        transactionDao.reduceAmount(senderId, receiverIds, groupIds) // deletes record
        Log.d(TAG, "settle: selected local datasource ${receiverIds}")
    }

    override suspend fun settle(groupId: Int, payerId: Int, recipientId: Int, amount: Float) { //
        // transactionDao.getAmount(groupId, payerId, recipientId)
       getOwed(payerId, listOf(recipientId), listOf(groupId))?.let {
            //transactionDao.updateAmount(groupId, payerId, recipientId, it - amount) // old
           transactionDao.updateAmount(groupId, recipientId, payerId, it - amount) // new
        }
    }

    override suspend fun settleAllInGroup(senderId: Int, groupId: Int) {
        transactionDao.reduceBulkAmountInGroup(senderId, groupId) // deletes record
    }

    override suspend fun settleAll(senderId: Int) {
        transactionDao.reduceBulkAmount(senderId) // deletes record
    }

    override suspend fun transactionStats(): List<MemberPaymentStats>? {
        val lendList = transactionDao.getLendAmount()
        val owedList = transactionDao.getOwedAmount()

        return if(lendList != null && owedList != null)
            listMerger(lendList, owedList)
        else{
            //throw NullPointerException("Transaction missing")
            null
        }
    }

    override suspend fun transactionStats(groupId: Int): List<MemberPaymentStats>? {
        val lendList = transactionDao.getLendAmountInGroup(groupId)
        val owedList = transactionDao.getOwedAmountInGroup(groupId)

        return if(lendList != null && owedList != null) {
            Log.d(TAG, "transactionStats: transaction found")
            listMerger(lendList, owedList)
        }
        else{
            Log.d(TAG, "transactionStats: null found")
            //throw NullPointerException("Transaction missing")
            null
        }
    }

    override suspend fun transactionStats(groupIds: List<Int>): List<MemberPaymentStats>? {
        val lendList = transactionDao.getLendAmountInGroups(groupIds)
        val owedList = transactionDao.getOwedAmountInGroups(groupIds)

        return if(lendList != null && owedList != null) {
            Log.d(TAG, "transactionStats: transaction found")
            listMerger(lendList, owedList)
        }
        else{
            Log.d(TAG, "transactionStats: null found")
            //throw NullPointerException("Transaction missing")
            null
        }
    }

    override suspend fun getOwed(senderId: Int, receiverId: Int): Float? {
        return transactionDao.getOwedAmount(senderId, receiverId)
    }

    override suspend fun getOwed(senderId: Int): Float? {
        return transactionDao.getOwedAmount(senderId)
    }

    override suspend fun getOwed(
        senderId: Int,
        receiverIds: List<Int>,
        groupIds: List<Int>
    ): Float? {
        return transactionDao.getOwedAmount(senderId, receiverIds, groupIds)
    }

    override suspend fun getOwedInGroup(senderId: Int, receiverId: Int, groupId: Int): Float? {
        return transactionDao.getOwedAmountInGroup(senderId, receiverId, groupId)
    }

    override suspend fun getOwedInGroup(senderId: Int, groupId: Int): Float? {
        return transactionDao.getOwedAmountInGroup(senderId, groupId)
    }

    override suspend fun getPayers(payeeId: Int): List<Int>? {
        return transactionDao.getPayers(payeeId)
    }

    override suspend fun getPayers(payeeId: Int, groupId: Int): List<Int>? {
        return transactionDao.getPayers(payeeId, groupId)
    }

    override suspend fun getPayers(payeeId: Int, groupIds: List<Int>): List<Int>? {
        return transactionDao.getPayers(payeeId, groupIds)
    }

    override suspend fun getAmount(groupId: Int, payerId: Int, payeeId: Int): Float? {
        return transactionDao.getAmount(groupId, payerId, payeeId)
    }

    override suspend fun updateAmount(groupId: Int, payerId: Int, payeeId: Int, amount: Float) { //
        transactionDao.updateAmount(groupId, payerId, payeeId, amount)
    }

    override suspend fun deleteGroupTransactions(groupId: Int) {
        transactionDao.deleteGroup(groupId)
    }

    private suspend fun listMerger(lendList: List<MemberAmount>, owedList: List<MemberAmount>): List<MemberPaymentStats>{
        Log.d(TAG, "listMerger: lend: $lendList \n owed: $owedList")
        return withContext(Dispatchers.IO){
            val memberPaymentStats = mutableListOf<MemberPaymentStats>()
            val lendMap = mutableMapOf<Int, Float>()

            for(i in lendList)
                lendMap[i.memberId] = i.amount

            for(i in owedList){
                if(lendMap.containsKey(i.memberId)){
                    memberPaymentStats.add(
                        MemberPaymentStats(i.memberId, lendMap[i.memberId] ?: 0F, i.amount)
                    )

                    lendMap.remove(i.memberId)
                }
                else{
                    memberPaymentStats.add(
                        MemberPaymentStats(i.memberId, 0F, i.amount)
                    )
                }
            }

            for( (key, value) in lendMap.entries)
                memberPaymentStats.add(
                    MemberPaymentStats(key, value, 0F)
                )

            Log.d(TAG, "listMerger: $memberPaymentStats")

            memberPaymentStats
        }

    }
}