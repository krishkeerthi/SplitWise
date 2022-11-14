package com.example.splitwise.framework.db.transaction

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.example.androidcore.data.transaction.MyTransactionDataSource
import com.example.androidcore.domain.transaction.MemberPaymentStatsModel
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.dao.TransactionDao
import com.example.splitwise.model.MemberAmount
import com.example.splitwise.model.MemberPaymentStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyTransactionLocalDataSource(context: Context): MyTransactionDataSource {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val transactionDao: TransactionDao = database.transactionDao()

    override suspend fun addTransaction(groupId: Int, payerId: Int, payeeId: Int, amount: Float) {
        TODO("Not yet implemented")
    }

    override suspend fun updateTransaction(
        groupId: Int,
        payerId: Int,
        payeeId: Int,
        amount: Float
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun settle(senderId: Int, receiverId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun settle(senderId: Int, receiverId: Int, groupId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun settle(senderId: Int, receiverIds: List<Int>, groupIds: List<Int>) {
        TODO("Not yet implemented")
    }

    override suspend fun settle(groupId: Int, payerId: Int, recipientId: Int, amount: Float) {
        TODO("Not yet implemented")
    }

    override suspend fun settleAllInGroup(senderId: Int, groupId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun settleAll(senderId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun transactionStats(): List<MemberPaymentStatsModel>? {
        val lendList = transactionDao.getLendAmount()
        val owedList = transactionDao.getOwedAmount()

        return if (lendList != null && owedList != null) {
            val memberPaymentStatsList = listMerger(lendList, owedList)
            val memberPaymentStatsModelList = mutableListOf<MemberPaymentStatsModel>()

            for(item in memberPaymentStatsList){
                memberPaymentStatsModelList.add(
                    MemberPaymentStatsModel(
                        item.memberId, item.amountLend, item.amountOwed
                    )
                )
            }

            memberPaymentStatsModelList
        }
        else {
            //throw NullPointerException("Transaction missing")
            null
        }
    }

    override suspend fun transactionStats(groupId: Int): List<MemberPaymentStatsModel>? {
        val lendList = transactionDao.getLendAmountInGroup(groupId)
        val owedList = transactionDao.getOwedAmountInGroup(groupId)

        return if (lendList != null && owedList != null) {
            val memberPaymentStatsList = listMerger(lendList, owedList)
            val memberPaymentStatsModelList = mutableListOf<MemberPaymentStatsModel>()

            for(item in memberPaymentStatsList){
                memberPaymentStatsModelList.add(
                    MemberPaymentStatsModel(
                        item.memberId, item.amountLend, item.amountOwed
                    )
                )
            }

            memberPaymentStatsModelList
        }
        else {
            //throw NullPointerException("Transaction missing")
            null
        }
    }

    override suspend fun transactionStats(groupIds: List<Int>): List<MemberPaymentStatsModel>? {
        val lendList = transactionDao.getLendAmountInGroups(groupIds)
        val owedList = transactionDao.getOwedAmountInGroups(groupIds)

        return if (lendList != null && owedList != null) {
            val memberPaymentStatsList = listMerger(lendList, owedList)
            val memberPaymentStatsModelList = mutableListOf<MemberPaymentStatsModel>()

            for(item in memberPaymentStatsList){
                memberPaymentStatsModelList.add(
                    MemberPaymentStatsModel(
                        item.memberId, item.amountLend, item.amountOwed
                    )
                )
            }

            memberPaymentStatsModelList
        }
        else {
            //throw NullPointerException("Transaction missing")
            null
        }
    }

    override suspend fun getOwed(senderId: Int, receiverId: Int): Float? {
        TODO("Not yet implemented")
    }

    override suspend fun getOwed(senderId: Int): Float? {
        TODO("Not yet implemented")
    }

    override suspend fun getOwed(
        senderId: Int,
        receiverIds: List<Int>,
        groupIds: List<Int>
    ): Float? {
        TODO("Not yet implemented")
    }

    override suspend fun getOwedInGroup(senderId: Int, receiverId: Int, groupId: Int): Float? {
        TODO("Not yet implemented")
    }

    override suspend fun getOwedInGroup(senderId: Int, groupId: Int): Float? {
        TODO("Not yet implemented")
    }

    override suspend fun getPayers(payeeId: Int): List<Int>? {
        TODO("Not yet implemented")
    }

    override suspend fun getPayers(payeeId: Int, groupId: Int): List<Int>? {
        TODO("Not yet implemented")
    }

    override suspend fun getPayers(payeeId: Int, groupIds: List<Int>): List<Int>? {
        TODO("Not yet implemented")
    }

    override suspend fun getAmount(groupId: Int, payerId: Int, payeeId: Int): Float? {
        TODO("Not yet implemented")
    }

    override suspend fun newUpdateAmount(groupId: Int, payerId: Int, payeeId: Int, amount: Float) {
        TODO("Not yet implemented")
    }

    override suspend fun updateAmount(groupId: Int, payerId: Int, payeeId: Int, amount: Float) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGroupTransactions(groupId: Int) {
        TODO("Not yet implemented")
    }

    private suspend fun listMerger(
        lendList: List<MemberAmount>,
        owedList: List<MemberAmount>
    ): List<MemberPaymentStats> {
        Log.d(ContentValues.TAG, "listMerger: lend: $lendList \n owed: $owedList")
        return withContext(Dispatchers.IO) {
            val memberPaymentStats = mutableListOf<MemberPaymentStats>()
            val lendMap = mutableMapOf<Int, Float>()

            for (i in lendList)
                lendMap[i.memberId] = i.amount

            for (i in owedList) {
                if (lendMap.containsKey(i.memberId)) {
                    memberPaymentStats.add(
                        MemberPaymentStats(i.memberId, lendMap[i.memberId] ?: 0F, i.amount)
                    )

                    lendMap.remove(i.memberId)
                } else {
                    memberPaymentStats.add(
                        MemberPaymentStats(i.memberId, 0F, i.amount)
                    )
                }
            }

            for ((key, value) in lendMap.entries)
                memberPaymentStats.add(
                    MemberPaymentStats(key, value, 0F)
                )

            Log.d(ContentValues.TAG, "listMerger: $memberPaymentStats")

            memberPaymentStats
        }

    }

}