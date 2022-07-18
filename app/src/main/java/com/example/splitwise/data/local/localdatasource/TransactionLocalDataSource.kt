package com.example.splitwise.data.local.localdatasource

import com.example.splitwise.data.datasource.TransactionDataSource
import com.example.splitwise.data.local.dao.TransactionDao
import com.example.splitwise.model.MemberAmount
import com.example.splitwise.model.MemberPaymentStats

class TransactionLocalDataSource(
    private val transactionDao: TransactionDao
): TransactionDataSource {
    override suspend fun settle(senderId: Int, receiverId: Int) {
        transactionDao.reduceAmount(senderId, receiverId)
    }

    override suspend fun settle(senderId: Int, receiverId: Int, groupId: Int) {
        transactionDao.reduceAmount(senderId, receiverId, groupId)
    }

    override suspend fun settleAllInGroup(senderId: Int, groupId: Int) {
        transactionDao.reduceBulkAmountInGroup(senderId, groupId)
    }

    override suspend fun settleAll(senderId: Int) {
        transactionDao.reduceBulkAmount(senderId)
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

        return if(lendList != null && owedList != null)
            listMerger(lendList, owedList)
        else{
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

    private fun listMerger(lendList: List<MemberAmount>, owedList: List<MemberAmount>): List<MemberPaymentStats>{
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

        return memberPaymentStats
    }
}