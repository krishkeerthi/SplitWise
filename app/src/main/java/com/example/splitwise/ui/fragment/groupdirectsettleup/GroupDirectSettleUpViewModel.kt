package com.example.splitwise.ui.fragment.groupdirectsettleup

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.splitwise.R
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.example.splitwise.data.repository.TransactionRepository
import com.example.splitwise.util.roundOff
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupDirectSettleUpViewModel(
    private val context: Context,
    val groupId: Int
): ViewModel() {

    private val database = SplitWiseRoomDatabase.getInstance(context)
    private val transactionRepository = TransactionRepository(database)
    private val memberRepository = MemberRepository(database)
    private val groupRepository = GroupRepository(database)

    private val _groupMembers = MutableLiveData<List<Member>?>()
    val groupMembers : LiveData<List<Member>?>
    get() = _groupMembers

    private val _payer = MutableLiveData<Member?>()
    val payer: LiveData<Member?>
        get() = _payer

    private val _recipient = MutableLiveData<Member?>()
    val recipient: LiveData<Member?>
        get() = _recipient

    private val _owedText = MutableLiveData<String?>()
    val owedText: LiveData<String?>
    get() = _owedText

    var payerId: Int = -1
    var recipientId: Int = -1
    var totalPayable = 0f

    init {
        viewModelScope.launch {
            fetchGroupMembers()
        }
    }
    private suspend fun fetchGroupMembers(){
        val members = groupRepository.getGroupMembers(groupId)

        _groupMembers.value = getMembersFromIds(members)
    }


    fun fetchOwedAmountText(){
        viewModelScope.launch {
            val payer = memberRepository.getMember(payerId)
            val recipient = memberRepository.getMember(recipientId)

            Log.d(TAG, "fetchOwedAmountText:  both are selected $payerId $recipientId")
            //val owedAmount = transactionRepository.getAmount(groupId, payerId, recipientId)
            val owedAmount = transactionRepository.getOwed(payerId, listOf(recipientId), listOf(groupId))

            totalPayable = owedAmount ?: 0f

            if(owedAmount != null){
                Log.d(TAG, "fetchOwedAmountText:  both are selected owed amount not null $payerId $recipientId")
                _owedText.value = String.format(context.resources.getString(R.string.owes_text),
                    payer?.name,
                    owedAmount.roundOff(),
                    recipient?.name
                    )
                    //"${payer?.name} owes ₹$owedAmount to ${recipient?.name}"
            }
            else
            {
                _owedText.value = String.format(context.resources.getString(R.string.owes_text),
                    payer?.name,
                    0,
                    recipient?.name
                )
                    //"${payer?.name} owes ₹0 to ${recipient?.name}"
            }

        }
    }

    fun fetchMember(memberId: Int, isPayer: Boolean){
        viewModelScope.launch {
            val member = memberRepository.getMember(memberId)

            if(isPayer)
                _payer.value = member
            else
                _recipient.value = member
        }
    }

    fun settle(amount: Float, onSettled: () -> Unit){
        viewModelScope.launch {
            //transactionRepository.settle(groupId, recipientId, payerId, amount)  // new
              transactionRepository.settle(groupId, payerId, recipientId, amount) // old

            onSettled()
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

}

class GroupDirectSettleUpViewModelFactory(
    private val context: Context, private val groupId: Int)
    :ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupDirectSettleUpViewModel(context, groupId) as T
    }
    }