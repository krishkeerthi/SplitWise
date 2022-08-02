package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.databinding.GroupCard1Binding
import com.example.splitwise.databinding.GroupCardBinding
import com.example.splitwise.util.formatDate
import com.example.splitwise.util.roundOff

class GroupsAdapter(
    val onExpenseClicked: (Int) -> Unit
) : RecyclerView.Adapter<GroupsViewHolder>() {
    private var groups = listOf<Group>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = GroupCard1Binding.inflate(view, parent, false)

        return GroupsViewHolder(binding).apply {
            itemView.setOnClickListener {
                onExpenseClicked(groups[adapterPosition].groupId)
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

    fun updateGroups(groups: List<Group>) {
        this.groups = groups
        notifyDataSetChanged()
    }
}

class GroupsViewHolder(val binding: GroupCard1Binding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(group: Group) {
        binding.groupNameTextView.text = group.groupName
        binding.groupExpenseTextView.text = "₹" + group.totalExpense.roundOff()
        binding.groupCreationDateTextView.text = formatDate(group.creationDate)
    }

}