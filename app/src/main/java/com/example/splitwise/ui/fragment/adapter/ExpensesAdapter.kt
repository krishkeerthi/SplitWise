package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.databinding.ExpenseCard1Binding
import com.example.splitwise.model.ExpenseMember
import com.example.splitwise.util.getCategoryDrawableResource
import com.example.splitwise.util.roundOff

class ExpensesAdapter(
    val onExpenseClicked: (Int) -> Unit
) : RecyclerView.Adapter<ExpensesViewHolder>() {
    private var expenseMembers = listOf<ExpenseMember>() // Expense And Member

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpensesViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = ExpenseCard1Binding.inflate(view, parent, false)

        return ExpensesViewHolder(binding).apply {
            itemView.setOnClickListener {
                onExpenseClicked(expenseMembers[adapterPosition].expenseId)
            }
        }
    }

    override fun onBindViewHolder(holder: ExpensesViewHolder, position: Int) {
        val expenseMember = expenseMembers[position]

        holder.bind(expenseMember)
    }

    override fun getItemCount(): Int {
        return expenseMembers.size
    }

    fun updateExpenseMembers(expenseMembers: List<ExpenseMember>) {
        this.expenseMembers = expenseMembers
        notifyDataSetChanged()
    }
}

class ExpensesViewHolder(val binding: ExpenseCard1Binding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(expenseMember: ExpenseMember) {
        binding.expenseImageView.setImageResource(getCategoryDrawableResource(expenseMember.category))
        binding.expenseNameTextView.text = expenseMember.expenseName
        binding.totalExpenseTextView.text = "₹" + expenseMember.totalAmount.roundOff()
        binding.expensePayerTextView.text = expenseMember.payerInfo.name
    }

}