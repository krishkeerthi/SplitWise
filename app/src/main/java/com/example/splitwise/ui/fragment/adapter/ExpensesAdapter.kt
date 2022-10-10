package com.example.splitwise.ui.fragment.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.databinding.EmptyCardBinding
import com.example.splitwise.databinding.ExpenseCard1Binding
import com.example.splitwise.model.ExpenseMember
import com.example.splitwise.util.*

class ExpensesAdapter(
    val onExpenseClicked: (Int, View) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var expenseMembers = listOf<ExpenseMember>() // Expense And Member

    private val EXPENSEVIEW = 1
    private val EMPTYVIEW = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder : RecyclerView.ViewHolder
        val view = LayoutInflater.from(parent.context)
        //val binding = ExpenseCard1Binding.inflate(view, parent, false)

        viewHolder = when(viewType){
            EXPENSEVIEW -> {
                val binding = ExpenseCard1Binding.inflate(view, parent, false)
                ExpensesViewHolder(binding).apply {
                    itemView.setOnClickListener {
                        //binding.expenseCard.ripple(binding.root.context)
                        itemView.ripple(it.context)
                        onExpenseClicked(expenseMembers[adapterPosition].expenseId, itemView)
                    }
                }
            }
            EMPTYVIEW ->{
                val binding = EmptyCardBinding.inflate(view, parent, false)
                ExpenseEmptyViewHolder(binding)
            }
            else -> {
                val binding = ExpenseCard1Binding.inflate(view, parent, false)
                ExpensesViewHolder(binding).apply {
                    itemView.setOnClickListener {
                        //binding.expenseCard.ripple(binding.root.context)
                        itemView.ripple(it.context)
                        onExpenseClicked(expenseMembers[adapterPosition].expenseId, itemView)
                    }
                }
            }
        }
//        return ExpensesViewHolder(binding).apply {
//        itemView.setOnClickListener {
//            //binding.expenseCard.ripple(binding.root.context)
//            itemView.ripple(it.context)
//            onExpenseClicked(expenseMembers[adapterPosition].expenseId, itemView)
//        }
//    }

        return viewHolder
    }

//    override fun onBindViewHolder(holder: ExpensesViewHolder, position: Int) {
//        val expenseMember = expenseMembers[position]
//
//        holder.bind(expenseMember)
//    }

    override fun getItemCount(): Int {
        return expenseMembers.size
    }

    fun updateExpenseMembers(expenseMembers: List<ExpenseMember>) {
        val updatedExpenseMembers = addEmptyMember(expenseMembers)
        this.expenseMembers = updatedExpenseMembers//expenseMembers
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if(expenseMembers[position].expenseId != -1)
            EXPENSEVIEW
        else
            EMPTYVIEW
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType){
            EXPENSEVIEW -> {
                val holder = holder as ExpensesViewHolder
                val expenseMember = expenseMembers[position]
                holder.bind(expenseMember)
            }
            EMPTYVIEW ->{
                val viewHolder = holder as ExpenseEmptyViewHolder
                viewHolder.bind()
            }
        }
    }

    private fun addEmptyMember(members: List<ExpenseMember>): List<ExpenseMember>{
        val mutableMembers = members.toMutableList()
        mutableMembers.add(dummyMemberExpense)
        return mutableMembers.toList()
    }
}

class ExpensesViewHolder(val binding: ExpenseCard1Binding) :
    RecyclerView.ViewHolder(binding.root) {

    val resources = binding.root.resources

    fun bind(expenseMember: ExpenseMember) {

        binding.expenseImageView.setImageBitmap(null)
        // load image after sometime
        //Handler(Looper.getMainLooper()).postDelayed({
            binding.expenseImageView.setImageResource(getCategoryDrawableResource(expenseMember.category))
        //}, resources.getInteger(R.integer.reply_motion_duration_medium).toLong())

        // transition name
        ViewCompat.setTransitionName(
            binding.root,
            String.format(resources.getString(R.string.expense_card_transition_name), expenseMember.expenseId)
        )

        binding.expenseNameTextView.text = expenseMember.expenseName
        binding.totalExpenseTextView.text = "₹" + expenseMember.totalAmount.roundOff()
        binding.expensePayerTextView.text = expenseMember.payerInfo.name
    }
}

class ExpenseEmptyViewHolder(val binding: EmptyCardBinding):
        RecyclerView.ViewHolder(binding.root){

            fun bind(){

            }
        }