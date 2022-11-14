package com.example.splitwise.ui.fragment.groups

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.androidcore.data.group.MyGroupDataSource
import com.example.androidcore.data.group.MyGroupRepository
import com.example.androidcore.domain.group.GroupModel
import com.example.androidcore.interactors.group.GroupInteractors
import com.example.splitwise.framework.Interactors
import getOrAwaitValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@Config(sdk = [30])
@RunWith(AndroidJUnit4::class)
class MyGroupsViewModelTest {

    private lateinit var myGroupsViewModel: MyGroupsViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        val testInteractors = getInteractors()
        myGroupsViewModel = MyGroupsViewModel(ApplicationProvider.getApplicationContext(), testInteractors)
    }

    @Test
    fun checkTestMethod() {

        myGroupsViewModel.testMethod(true)

        val value = myGroupsViewModel.testLiveData.getOrAwaitValue()

        assertThat(value, `is` (true))
    }


    //@Test
    fun getInteractors(): Interactors{

        val testGroupDataSource = object : MyGroupDataSource{
            override suspend fun createGroup(
                name: String,
                description: String,
                date: Date,
                expense: Float,
                icon: Uri?
            ): Int {
                TODO("Not yet implemented")
            }

            override suspend fun addGroupMember(groupId: Int, memberId: Int) {
                TODO("Not yet implemented")
            }

            override suspend fun getGroupMembers(groupId: Int): List<Int>? {
                TODO("Not yet implemented")
            }

            override suspend fun addGroupExpense(groupId: Int, expenseId: Int) {
                TODO("Not yet implemented")
            }

            override suspend fun getGroup(groupId: Int): GroupModel? {
                TODO("Not yet implemented")
            }

            override suspend fun getGroups(): List<GroupModel>? {
                TODO("Not yet implemented")
            }

            override suspend fun getGroups(groupIds: List<Int>): List<GroupModel>? {
                TODO("Not yet implemented")
            }

            override suspend fun getGroupsStartsWith(query: String): List<GroupModel>? {
                TODO("Not yet implemented")
            }

            override suspend fun getGroupsContains(query: String): List<GroupModel>? {
                TODO("Not yet implemented")
            }

            override suspend fun updateTotalExpense(groupId: Int, amount: Float) {
                TODO("Not yet implemented")
            }

            override suspend fun getTotalExpense(groupId: Int): Float? {
                TODO("Not yet implemented")
            }

            override suspend fun getGroupsCreatedBefore(date: Date): List<GroupModel>? {
                TODO("Not yet implemented")
            }

            override suspend fun getGroupsCreatedAfter(date: Date): List<GroupModel>? {
                TODO("Not yet implemented")
            }

            override suspend fun getGroupsWithAmountBelow(amount: Float): List<GroupModel>? {
                TODO("Not yet implemented")
            }

            override suspend fun getGroupsWithAmountAbove(amount: Float): List<GroupModel> {
                TODO("Not yet implemented")
            }

            override suspend fun getGroupsCreatedBeforeAndAmountBelow(
                date: Date,
                amount: Float
            ): List<GroupModel>? {
                TODO("Not yet implemented")
            }

            override suspend fun getGroupsCreatedBeforeAndAmountAbove(
                date: Date,
                amount: Float
            ): List<GroupModel>? {
                TODO("Not yet implemented")
            }

            override suspend fun getGroupsCreatedAfterAndAmountBelow(
                date: Date,
                amount: Float
            ): List<GroupModel>? {
                TODO("Not yet implemented")
            }

            override suspend fun getGroupsCreatedAfterAndAmountAbove(
                date: Date,
                amount: Float
            ): List<GroupModel>? {
                TODO("Not yet implemented")
            }

            override suspend fun deleteGroup(groupId: Int) {
                TODO("Not yet implemented")
            }

            override suspend fun updateGroupIcon(groupId: Int, uri: Uri) {
                TODO("Not yet implemented")
            }

            override suspend fun updateGroupName(groupId: Int, groupName: String) {
                TODO("Not yet implemented")
            }

            override suspend fun removeGroup(groupId: Int) {
                TODO("Not yet implemented")
            }

            override suspend fun removeGroupMembers(groupId: Int) {
                TODO("Not yet implemented")
            }

            override suspend fun removeGroupIcon(groupId: Int) {
                TODO("Not yet implemented")
            }

        }

        val testGroupRepository = MyGroupRepository(
            testGroupDataSource
        )
        val interactor = Interactors(
            GroupInteractors(
                testGroupRepository
            )
        )
        return interactor
    }
}

