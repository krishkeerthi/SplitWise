package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.databinding.ExpenseCard1Binding
import com.example.splitwise.databinding.ExpenseCardBinding
import com.example.splitwise.model.ExpenseMember
import com.example.splitwise.util.Category
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
        binding.expenseImageView.setImageResource(getDrawableResource(expenseMember.category))
        binding.expenseNameTextView.text = expenseMember.expenseName
        binding.totalExpenseTextView.text = "₹" + expenseMember.totalAmount.roundOff()
        binding.expensePayerTextView.text = expenseMember.payerInfo.name
    }

    private fun getDrawableResource(category: Int): Int {
        return when(category){
            Category.FOOD.ordinal -> R.drawable.food
            Category.OTHERS.ordinal -> R.drawable.others
            Category.TRAVEL.ordinal -> R.drawable.travel
            Category.RENT.ordinal -> R.drawable.rent
            Category.FEES.ordinal -> R.drawable.fees
            Category.REPAIRS.ordinal -> R.drawable.repair
            Category.ESSENTIALS.ordinal -> R.drawable.essentials
            Category.ENTERTAINMENT.ordinal -> R.drawable.entertainment
            Category.TICKETS.ordinal -> R.drawable.tickets
            else -> R.drawable.others
        }
    }

}