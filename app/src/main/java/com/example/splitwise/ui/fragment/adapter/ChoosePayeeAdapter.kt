package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.ChoosePayeeCardBinding

class ChoosePayeeAdapter(val onItemChecked: (Member, Boolean) -> Unit) :
    RecyclerView.Adapter<ChoosePayeeViewHolder>() {
    private var payees = listOf<Member>()
    private var selectedPayeesId = listOf<Int>()
    private var selectedAllPayees: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoosePayeeViewHolder{
        val view = LayoutInflater.from(parent.context)
        val binding = ChoosePayeeCardBinding.inflate(view, parent, false)

        return ChoosePayeeViewHolder(binding).apply {
            binding.selectedCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked)
                    onItemChecked(payees[adapterPosition], true)
                else
                    onItemChecked(payees[adapterPosition], false)
            }
        }
    }

    override fun onBindViewHolder(holder: ChoosePayeeViewHolder, position: Int) {
        val payee = payees[position]

        if(!selectedAllPayees)
            holder.bind(payee, selectedPayeesId)
        else
            holder.bindAndCheck(payee)
    }

    override fun getItemCount(): Int {
        return payees.size
    }

    fun selectAllPayees() {
        selectedAllPayees = true
        notifyDataSetChanged()
    }

    fun updatePayees(payees: List<Member>, selectedPayeesId: List<Int>) {
        this.payees= payees
        this.selectedPayeesId = selectedPayeesId
        notifyDataSetChanged()
    }
}

class ChoosePayeeViewHolder(val binding: ChoosePayeeCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(payee: Member, selectedPayees: List<Int>) {
        binding.payeeNameTextView.text = payee.name

        if(payee.memberId in selectedPayees)
            binding.selectedCheckBox.isChecked = true
    }

    fun bindAndCheck(payee: Member) {
        binding.payeeNameTextView.text = payee.name
        binding.selectedCheckBox.isChecked = true
    }
}