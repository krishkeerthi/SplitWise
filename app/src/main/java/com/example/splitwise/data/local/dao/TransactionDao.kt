package com.example.splitwise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.splitwise.model.MemberAmount
import com.example.splitwise.data.local.entity.Transaction

@Dao
interface TransactionDao {

    @Query("SELECT payer as memberId, SUM(amount) as amount FROM `transaction` GROUP BY payer")
    suspend fun getLendAmount(): List<MemberAmount>?

    @Query("SELECT payer as memberId, SUM(amount) as amount FROM `transaction` WHERE group_id = :groupId GROUP BY payer")
    suspend fun getLendAmountInGroup(groupId: Int): List<MemberAmount>?

    @Query("SELECT payer as memberId, SUM(amount) as amount FROM `transaction` WHERE group_id IN (:groupIds) GROUP BY payer")
    suspend fun getLendAmountInGroups(groupIds: List<Int>): List<MemberAmount>?

    @Query("SELECT payee as memberId, SUM(amount) as amount FROM `transaction` GROUP BY payee")
    suspend fun getOwedAmount(): List<MemberAmount>?

    @Query("SELECT payee as memberId, SUM(amount) as amount FROM `transaction` WHERE group_id = :groupId GROUP BY payee")
    suspend fun getOwedAmountInGroup(groupId: Int): List<MemberAmount>?

    @Query("SELECT payee as memberId, SUM(amount) as amount FROM `transaction` WHERE group_id IN (:groupIds) GROUP BY payee")
    suspend fun getOwedAmountInGroups(groupIds: List<Int>): List<MemberAmount>?

    @Query("SELECT SUM(amount) FROM `transaction` WHERE payer = :receiverId AND payee = :senderId AND group_id = :groupId")
    suspend fun getOwedAmountInGroup(senderId: Int, receiverId: Int, groupId: Int): Float?

    @Query("SELECT SUM(amount) FROM `transaction` WHERE payer = :receiverId AND payee = :senderId")
    suspend fun getOwedAmount(senderId: Int, receiverId: Int): Float?

    @Query("SELECT SUM(amount) FROM `transaction` WHERE payer IN (:receiverIds) AND payee = :senderId AND group_id IN (:groupIds)")
    suspend fun getOwedAmount(senderId: Int, receiverIds: List<Int>, groupIds: List<Int>): Float?

    @Query("SELECT SUM(amount) FROM `transaction` WHERE payee = :senderId AND group_id = :groupId")
    suspend fun getOwedAmountInGroup(senderId: Int, groupId: Int): Float?

    @Query("SELECT SUM(amount) FROM `transaction` WHERE payee = :senderId")
    suspend fun getOwedAmount(senderId: Int): Float?

    @Query("SELECT payer FROM `transaction` WHERE payee = :payeeId")
    suspend fun getPayers(payeeId: Int): List<Int>?

    @Query("SELECT payer FROM `transaction` WHERE payee = :payeeId AND group_id = :groupId")
    suspend fun getPayers(payeeId: Int, groupId: Int): List<Int>?

    @Query("SELECT payer FROM `transaction` WHERE payee = :payeeId AND group_id IN (:groupIds)")
    suspend fun getPayers(payeeId: Int, groupIds: List<Int>): List<Int>?

    @Query("DELETE FROM `transaction` WHERE payer = :receiverId AND payee = :senderId")
    suspend fun reduceAmount(senderId: Int, receiverId: Int)

    @Query("DELETE FROM `transaction` WHERE payer = :receiverId AND payee = :senderId AND group_id = :groupId")
    suspend fun reduceAmount(senderId: Int, receiverId: Int, groupId: Int)

    @Query("DELETE FROM `transaction` WHERE payer IN (:receiverIds) AND payee = :senderId AND group_id IN (:groupIds)") //
    suspend fun reduceAmount(senderId: Int, receiverIds: List<Int>, groupIds: List<Int>)

    @Query("UPDATE `transaction` SET amount = :amount WHERE payer IN (:receiverIds) AND payee = :senderId AND group_id IN (:groupIds)") //
    suspend fun setAmount0(senderId: Int, receiverIds: List<Int>, groupIds: List<Int>, amount: Float)

    @Query("DELETE FROM `transaction` WHERE payee = :senderId")
    suspend fun reduceBulkAmount(senderId: Int)

    @Query("DELETE FROM `transaction` WHERE payee = :senderId AND group_id = :groupId")
    suspend fun reduceBulkAmountInGroup(senderId: Int, groupId: Int)

    @Query("SELECT amount FROM `transaction` WHERE group_id = :groupId AND payer= :payerId AND payee = :payeeId")
    suspend fun getAmount(groupId: Int, payerId: Int, payeeId: Int): Float?

    @Query("UPDATE `transaction` SET amount = :amount WHERE group_id = :groupId AND payer= :payerId AND payee = :payeeId")
    suspend fun updateAmount(groupId: Int, payerId: Int, payeeId: Int, amount: Float)

    @Query("SELECT * FROM `transaction`")
    suspend fun getTransactions(): List<Transaction>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Query("DELETE FROM `transaction` WHERE group_id = :groupId")
    suspend fun deleteGroup(groupId: Int)
}