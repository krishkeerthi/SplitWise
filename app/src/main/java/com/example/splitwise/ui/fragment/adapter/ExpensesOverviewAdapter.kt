package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.data.local.entity.Expense
import com.example.splitwise.databinding.ShareCardBinding

class ExpensesOverviewAdapter: RecyclerView.Adapter<ExpensesOverviewViewHolder>() {
    private var expenses = listOf<Expense>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpensesOverviewViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = ShareCardBinding.inflate(view, parent, false)

        return ExpensesOverviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpensesOverviewViewHolder, position: Int) {
        val expense = expenses[position]

        holder.bind(expense)
    }

    override fun getItemCount(): Int {
        return expenses.size
    }

    fun updateExpenses(expenses: List<Expense>){
        this.expenses = expenses
        notifyDataSetChanged()
    }
}

class ExpensesOverviewViewHolder(val binding: ShareCardBinding)
    : RecyclerView.ViewHolder(binding.root){

    fun bind(expense: Expense){
        binding.shareNameTextView.text = expense.expenseName
        binding.shareAmountTextView.text = expense.totalAmount.toString()
    }

}