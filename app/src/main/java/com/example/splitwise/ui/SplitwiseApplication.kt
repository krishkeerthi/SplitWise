package com.example.splitwise.ui

import android.app.Application
import com.example.androidcore.data.group.MyGroupRepository
import com.example.androidcore.data.member.MyMemberRepository
import com.example.androidcore.data.transaction.MyTransactionRepository
import com.example.androidcore.interactors.group.GroupInteractors
import com.example.androidcore.interactors.member.MemberInteractors
import com.example.androidcore.interactors.transaction.TransactionInteractors
import com.example.splitwise.data.local.localdatasource.GroupLocalDataSource
import com.example.splitwise.framework.Interactors
import com.example.splitwise.framework.SplitwiseViewModelFactory
import com.example.splitwise.framework.db.group.MyGroupLocalDataSource
import com.example.splitwise.framework.db.member.MyMemberLocalDataSource
import com.example.splitwise.framework.db.transaction.MyTransactionLocalDataSource

class SplitwiseApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        application = this

        val myGroupRepository = MyGroupRepository(MyGroupLocalDataSource(this))
        val myMemberRepository = MyMemberRepository(MyMemberLocalDataSource(this))
        val myTransactionRepository = MyTransactionRepository(MyTransactionLocalDataSource(this))

        SplitwiseViewModelFactory.inject(
            this,
            Interactors(
                GroupInteractors(myGroupRepository),
                MemberInteractors(myMemberRepository),
                TransactionInteractors(myTransactionRepository)
            )
        )
    }

    companion object {
        private lateinit var application: SplitwiseApplication

        fun getApplication() = application
    }
}