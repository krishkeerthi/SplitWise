package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.ChoosePayeeCardBinding
import com.example.splitwise.model.MemberAndAmount
import com.example.splitwise.util.roundOff

class ChoosePayeeAdapter(val onItemChecked: (Member, Boolean) -> Unit) :
    RecyclerView.Adapter<ChoosePayeeViewHolder>() {
    //private var payees = listOf<Member>()
    private var payeesAndAmounts = listOf<MemberAndAmount>()
    private var selectedPayeesId = listOf<Int>()
    private var selectedAllPayees: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoosePayeeViewHolder{
        val view = LayoutInflater.from(parent.context)
        val binding = ChoosePayeeCardBinding.inflate(view, parent, false)

        return ChoosePayeeViewHolder(binding).apply {
            itemView.setOnClickListener {
                val isChecked = binding.selectedCheckBox.isChecked
                binding.selectedCheckBox.isChecked = !isChecked
            }

            binding.selectedCheckBox.setOnCheckedChangeListener { compoundButton, isChecked ->
                // Log.d(TAG, "onCreateViewHolder: group checkbox checked $isChecked")
                if (isChecked) {
                    //binding.selectedCheckBox.isChecked = true
                    onItemChecked(payeesAndAmounts[adapterPosition].member, true)
                }
                else {
                    //binding.selectedCheckBox.isChecked = false
                    onItemChecked(payeesAndAmounts[adapterPosition].member, false)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ChoosePayeeViewHolder, position: Int) {
        val payee = payeesAndAmounts[position].member
        val amount = payeesAndAmounts[position].amount

        if(!selectedAllPayees)
            holder.bind(payee, amount, selectedPayeesId)
        else
            holder.bindAndCheck(payee)
    }

    override fun getItemCount(): Int {
        return payeesAndAmounts.size
    }

    fun selectAllPayees() {
        selectedAllPayees = true
        notifyDataSetChanged()
    }

    fun updatePayees(payeesAndAmounts: List<MemberAndAmount>, selectedPayeesId: List<Int>) {
        //this.payees= payees
        this.payeesAndAmounts = payeesAndAmounts
        this.selectedPayeesId = selectedPayeesId
        notifyDataSetChanged()
    }
}

class ChoosePayeeViewHolder(val binding: ChoosePayeeCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(payee: Member, amount: Float, selectedPayees: List<Int>) {
        binding.payeeNameTextView.text = payee.name
        binding.amountTextView.text = "₹" + amount.roundOff()

        if(payee.memberId in selectedPayees)
            binding.selectedCheckBox.isChecked = true
    }

    fun bindAndCheck(payee: Member) {
        binding.payeeNameTextView.text = payee.name
        binding.selectedCheckBox.isChecked = true
    }
}