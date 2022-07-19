package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.databinding.GroupCardBinding

class GroupsAdapter(
    val onAddMemberClicked: (Int) -> Unit,
    val onExpenseClicked: (Int) -> Unit
): RecyclerView.Adapter<GroupsViewHolder>() {
    private var groups = listOf<Group>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = GroupCardBinding.inflate(view, parent, false)

        return GroupsViewHolder(binding).apply {
            itemView.setOnClickListener {
                onExpenseClicked(groups[adapterPosition].groupId)
            }
            binding.addExpenseTextView.setOnClickListener {
                onExpenseClicked(groups[adapterPosition].groupId)
            }
            binding.addMembersTextView.setOnClickListener {
                onAddMemberClicked(groups[adapterPosition].groupId)
            }
        }
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        val group = groups[position]

        holder.bind(group)
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    fun updateGroups(groups: List<Group>){
        this.groups = groups
        notifyDataSetChanged()
    }
}

class GroupsViewHolder(val binding: GroupCardBinding)
    : RecyclerView.ViewHolder(binding.root){

        fun bind(group: Group){
            binding.groupNameTextView.text = group.groupName
            binding.groupExpenseTextView.text = group.totalExpense.toString()
        }

}