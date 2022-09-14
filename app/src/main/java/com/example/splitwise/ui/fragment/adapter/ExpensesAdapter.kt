package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.databinding.ExpenseCard1Binding
import com.example.splitwise.model.ExpenseMember
import com.example.splitwise.util.getCategoryDrawableResource
import com.example.splitwise.util.ripple
import com.example.splitwise.util.roundOff

class ExpensesAdapter(
    val onExpenseClicked: (Int, View) -> Unit
) : RecyclerView.Adapter<ExpensesViewHolder>() {
    private var expenseMembers = listOf<ExpenseMember>() // Expense And Member

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpensesViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = ExpenseCard1Binding.inflate(view, parent, false)

        return ExpensesViewHolder(binding).apply {
            itemView.setOnClickListener {
                //binding.expenseCard.ripple(binding.root.context)
                //itemView.ripple(itemView.context)
                onExpenseClicked(expenseMembers[adapterPosition].expenseId, itemView)
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

    val resources = binding.root.resources

    fun bind(expenseMember: ExpenseMember) {

        ViewCompat.setTransitionName(
            binding.root,
            String.format(resources.getString(R.string.expense_card_transition_name), expenseMember.expenseId)
        )

        binding.expenseImageView.setImageResource(getCategoryDrawableResource(expenseMember.category))
        binding.expenseNameTextView.text = expenseMember.expenseName
        binding.totalExpenseTextView.text = "₹" + expenseMember.totalAmount.roundOff()
        binding.expensePayerTextView.text = expenseMember.payerInfo.name
    }

}