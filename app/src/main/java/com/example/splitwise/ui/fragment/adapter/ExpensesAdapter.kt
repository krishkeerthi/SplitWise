package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.data.local.entity.Expense
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.ExpenseCardBinding
import com.example.splitwise.databinding.MemberCardBinding
import com.example.splitwise.model.ExpenseMember

class ExpensesAdapter: RecyclerView.Adapter<ExpensesViewHolder>() {
    private var expenseMembers = listOf<ExpenseMember>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpensesViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = ExpenseCardBinding.inflate(view, parent, false)

        return ExpensesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpensesViewHolder, position: Int) {
        val expenseMember = expenseMembers[position]

        holder.bind(expenseMember)
    }

    override fun getItemCount(): Int {
        return expenseMembers.size
    }

    fun updateExpenseMembers(expenseMembers: List<ExpenseMember>){
        this.expenseMembers = expenseMembers
        notifyDataSetChanged()
    }
}

class ExpensesViewHolder(val binding: ExpenseCardBinding)
    : RecyclerView.ViewHolder(binding.root){

    fun bind(expenseMember: ExpenseMember){
        binding.expenseNameTextView.text = expenseMember.expenseName
        binding.totalExpenseTextView.text = expenseMember.totalAmount.toString()
        binding.expensePayerTextView.text = expenseMember.payerInfo.name
    }

}