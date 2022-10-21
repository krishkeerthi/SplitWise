package com.example.splitwise.ui.fragment.expenses

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Expense
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.ExpenseRepository
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.data.repository.TransactionRepository
import com.example.splitwise.model.ExpenseMember
import com.example.splitwise.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue

class ExpensesViewModel(context: Context, val groupId: Int) : ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val groupRepository = GroupRepository(database)
    private val memberRepository = MemberRepository(database)
    private val expenseRepository = ExpenseRepository(database)
    private val transactionRepository = TransactionRepository(database)

    var clicked: Boolean = false

    private val _group = MutableLiveData<Group?>()

    val group: LiveData<Group?>
        get() = _group

    private val _groupMembers = MutableLiveData<List<Member>?>()

    var pending: Boolean = false
    var pendingCategory: Category? = null

    private val _running = MutableLiveData<Boolean>()
    val running: LiveData<Boolean>
        get() = _running

    val groupMembers: LiveData<List<Member>?>
        get() = _groupMembers

    private val _expenseMembers = MutableLiveData<List<ExpenseMember>?>()

    val expenseMembers: LiveData<List<ExpenseMember>?>
        get() = _expenseMembers

    private val _expenseCount = MutableLiveData<Int>(0)

    val expenseCount: LiveData<Int>
        get() = _expenseCount

    val checkedFilters = mutableSetOf<Category>()

    private val _checkedFiltersCount = MutableLiveData<Int>(0)

    val checkedFiltersCount: LiveData<Int>
        get() = _checkedFiltersCount

    init {
        //fetchData()
        //loadExpensesAndMembers()
        //loadMembers()
        _running.value = false
    }

//    fun fetchData() {
//        Log.d(TAG, "expenseviewmodel: created")
//        viewModelScope.launch {
//            _group.value = groupRepository.getGroup(groupId)
//            setExpenseMembers(true)
//            val memberIds = groupRepository.getGroupMembers(groupId)
//            Log.d(TAG, "viewmodel: group members $memberIds with groupid $groupId")
//            _groupMembers.value = getMembersFromIds(memberIds)
//
//        }
//    }

    fun loadMembers() {
        Log.d(TAG, "expenseviewmodel: created")
        viewModelScope.launch {

            loadGroup()
            //setExpenseMembers(true)
            loadGroupMembers()
        }
    }

    fun loadGroup() {
        viewModelScope.launch {
            _group.value = groupRepository.getGroup(groupId)
        }
    }

    private fun loadGroupMembers() {
        viewModelScope.launch {
            val memberIds = groupRepository.getGroupMembers(groupId)
            Log.d(TAG, "viewmodel: group members $memberIds with groupid $groupId")
            _groupMembers.value = getMembersFromIds(memberIds)
        }
    }

    fun loadExpensesAndMembers() { // previously private
        viewModelScope.launch {
            setExpenseMembers(true)
        }
    }

    private suspend fun getMembersFromIds(memberIds: List<Int>?): MutableList<Member>? {
        return withContext(Dispatchers.IO) {
            if (memberIds != null) {
                val members = mutableListOf<Member>()

                for (id in memberIds) {
                    val member = memberRepository.getMember(id)
                    member?.let {
                        members.add(it)
                    }
                }

                members
            } else
                null
        }
    }


    private suspend fun toExpenseMember(expense: Expense, member: Member): ExpenseMember {
        return withContext(Dispatchers.IO) {
            ExpenseMember(
                expense.expenseId,
                expense.groupId,
                expense.expenseName,
                expense.category,
                expense.totalAmount,
                expense.splitAmount,
                expense.payer,
                member,
                expense.date
            )
        }
    }

    private suspend fun setExpenseMembers(initialLoad: Boolean = false) {

        viewModelScope.launch {
            val ordinals = mutableListOf<Int>()
            for (category in checkedFilters) {
                ordinals.add(category.ordinal)
            }

//            val expenses = category?.let { expenseRepository.getExpensesByCategory(groupId, it.ordinal) }
//                ?: expenseRepository.getExpenses(groupId)

            val expenses = if (initialLoad || ordinals.isEmpty())
                expenseRepository.getExpenses(groupId)
            else
                expenseRepository.getExpensesByCategories(groupId, ordinals)

            if (expenses != null) {
                // setting expense count on initial load
                if (initialLoad)
                    _expenseCount.value = expenses.size

                Log.d(TAG, "viewmodel: group expenses $expenses")
                val expenseMembers = mutableListOf<ExpenseMember>()

                for (expense in expenses) {
                    val member = memberRepository.getMember(expense.payer)

                    if (member != null)
                        expenseMembers.add(toExpenseMember(expense, member))
                }

                _expenseMembers.value =
                    expenseMembers.reversed() // reversed the list here, instead of reversing the layout
            }
        }
    }

    fun deleteGroup(groupId: Int) {
        viewModelScope.launch {
            groupRepository.deleteGroup(groupId)
        }
    }

    fun filterByCategory() {
        if (_running.value == false)
            viewModelScope.launch {
                _running.value = true
                setExpenseMembers()
                _running.value = false

            }
        else {
            pending = true
            pendingCategory = Category.OTHERS // need to work
        }

    }

    fun decrementCheckedFiltersCount() {
        _checkedFiltersCount.value = _checkedFiltersCount.value?.minus(1)
    }

    fun incrementCheckedFiltersCount() {
        _checkedFiltersCount.value = _checkedFiltersCount.value?.plus(1)
    }

    fun generateReport(shareIntent: (String) -> Unit) {
        viewModelScope.launch {
            var report = "👋Hello\n"
            report += "\n😃Welcome to *SplitWise* ‼\n"
            // generating report

            val group = groupRepository.getGroup(groupId)

            group?.let { it ->
                report += "\n👨‍👩‍👧‍👦Group: ${it.groupName.trim().getBold()}"

                val membersId = groupRepository.getGroupMembers(it.groupId)
                val members = getMembersFromIds(membersId).printableMember()

                report += "\nMembers: $members"
                report += "\nDate: ${formatDate(it.creationDate, true)}\n"

                val expenses = expenseRepository.getExpenses(groupId)
                expenses?.let { expenses ->
                    for (expense in expenses) {
                        report += "\n${getEmoji(expense.category)}Expense: ${
                            expense.expenseName.trim().getBold()
                        }"

                        val expenseMembersId = expenseRepository.getExpensePayees(expense.expenseId)
                        val expenseMembers = getMembersFromIds(expenseMembersId).printableMember()

                        report += "\nPayees: $expenseMembers"

                        memberRepository.getMember(expense.payer)?.let {
                            report += "\nPayer: ${it.name.getBold()}"
                        }

                        report += "\nTotal: ₹ ${expense.totalAmount.roundOff()}"
                        report += "\nSplit Amount: ₹ ${expense.splitAmount.roundOff().getBold()}\n"
                    }
                }

                // deeplink
                report += "\nsplitwise.com/expenses/${it.groupId}"
            }

            report += "\n♥Thank you for using *SplitWise*"



            shareIntent(report)
        }
    }

//    fun clearCategoryFilter() {
//        viewModelScope.launch {
//            _pending.value = false
//            Log.d(TAG, "clearCategoryFilter: started")
//            setExpenseMembers()
//            Log.d(TAG, "clearCategoryFilter: ended")
//            _pending.value = true
//        }
//    }

    fun deleteExpense(groupId: Int, expenseId: Int, onDelete: () -> Unit) {
        viewModelScope.launch {
            //1. delete bills
            expenseRepository.deleteBills(expenseId)
            //2. delete group expense
            expenseRepository.removeExpenseIdFromGroup(groupId, expenseId)

            expenseRepository.getExpense(expenseId)?.let { expense ->
                // 3. decrement member streak payer
                //memberRepository.decrementStreak(expense.payer) //later ref

                expenseRepository.getExpensePayees(expenseId)?.let { payeesIds ->
                    // decrementing streak payees
                    for (payeeId in payeesIds)
                        memberRepository.decrementStreak(payeeId)

                    //4. reduce amount in transaction
                    for (payeeId in payeesIds) {
                        transactionRepository.getAmount(groupId, expense.payer, payeeId)
                            ?.let { amount ->
                                val updatedAmount = amount - expense.splitAmount
                                if (updatedAmount >= 0) {
                                    transactionRepository.updateAmount(
                                        groupId,
                                        expense.payer,
                                        payeeId,
                                        updatedAmount
                                    )
                                } else {
                                    transactionRepository.updateAmount(
                                        groupId,
                                        expense.payer,
                                        payeeId,
                                        0f
                                    )
                                    transactionRepository.updateAmount(
                                        groupId,
                                        payeeId,
                                        expense.payer,
                                        updatedAmount.absoluteValue
                                    )
                                }
                            }
                    }

                    //5. delete expense payees
                    for (payeeId in payeesIds)
                        expenseRepository.removeExpensePayee(expenseId, payeeId)

                }

                //6. reduce expense total in groups
                groupRepository.getTotalExpense(groupId)?.let { total ->
                    Log.d(
                        TAG,
                        "deleteExpense: group total ${total} expense total ${expense.totalAmount}"
                    )
                    groupRepository.reduceTotalExpense(groupId, total - expense.totalAmount)
                }
                //7. delete expense in expenses
                expenseRepository.deleteExpense(expenseId)

                onDelete()
            }

        }
    }

    fun deleteGroup(groupId: Int, onDelete: () -> Unit) {
        viewModelScope.launch {
            expenseRepository.getExpenses(groupId)?.let { expenses ->

                for (expense in expenses) {
                    //1. remove bills
                    expenseRepository.deleteBills(expense.expenseId)

                    // 2. decrement member streak payer
                    //memberRepository.decrementStreak(expense.payer) // later ref

                    expenseRepository.getExpensePayees(expense.expenseId)?.let { payeesIds ->
                        // decrementing streak payees
                        for (payeeId in payeesIds)
                            memberRepository.decrementStreak(payeeId)

                        //3. delete expense payees
                        for (payeeId in payeesIds)
                            expenseRepository.removeExpensePayee(expense.expenseId, payeeId)
                    }

                    //4. delete group expense
                    expenseRepository.removeExpenseIdFromGroup(groupId, expense.expenseId)

                    //5. delete expense
                    expenseRepository.deleteExpense(expense.expenseId)
                }

                //6. transactions
                transactionRepository.deleteGroupTransactions(groupId)

                // 7. remove group members
                groupRepository.removeGroupMembers(groupId)

                //8. remove group
                groupRepository.removeGroup(groupId)

                onDelete()
            }
        }
    }

}

class ExpensesViewModelFactory(private val context: Context, private val groupId: Int) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExpensesViewModel(context, groupId) as T
    }
}