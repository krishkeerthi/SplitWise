package com.example.splitwise.data.datasource

import com.example.splitwise.model.MemberPaymentStats

interface TransactionDataSource {

    suspend fun addTransaction(groupId: Int, payerId: Int, payeeId: Int, amount: Float)

    suspend fun updateTransaction(groupId: Int, payerId: Int, payeeId: Int, amount: Float)

    suspend fun settle(senderId: Int, receiverId: Int)

    suspend fun settle(senderId: Int, receiverId: Int, groupId: Int)

    suspend fun settle(senderId: Int, receiverIds: List<Int>, groupIds: List<Int>) //

    suspend fun settleAllInGroup(senderId: Int, groupId: Int)

    suspend fun settleAll(senderId: Int)

    suspend fun transactionStats(): List<MemberPaymentStats>?

    suspend fun transactionStats(groupId: Int): List<MemberPaymentStats>?

    suspend fun transactionStats(groupIds: List<Int>): List<MemberPaymentStats>?

    suspend fun getOwed(senderId: Int, receiverId: Int): Float?

    suspend fun getOwedInGroup(senderId: Int, receiverId: Int, groupId: Int): Float?

    suspend fun getOwed(senderId: Int): Float?

    suspend fun getOwed(senderId: Int, receiverIds: List<Int>, groupIds: List<Int>): Float?

    suspend fun getOwedInGroup(senderId: Int, groupId: Int): Float?

    suspend fun getPayers(payeeId: Int): List<Int>?

    suspend fun getPayers(payeeId: Int, groupId: Int): List<Int>?

    suspend fun getPayers(payeeId: Int, groupIds: List<Int>): List<Int>?

    suspend fun getAmount(groupId: Int, payerId: Int, payeeId: Int): Float?

    suspend fun newUpdateAmount(groupId: Int, payerId: Int, payeeId: Int, amount: Float) // later ref update amount

    suspend fun updateAmount(groupId: Int, payerId: Int, payeeId: Int, amount: Float)

    suspend fun deleteGroupTransactions(groupId: Int)

    suspend fun settle(groupId: Int, payerId: Int, recipientId: Int, amount: Float) //
}