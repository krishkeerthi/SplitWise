package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.databinding.GroupOverviewCardBinding
import com.example.splitwise.util.formatDate
import com.example.splitwise.util.roundOff

class GroupsOverviewAdapter(
    val onGroupOverviewClicked: (Int) -> Unit
) : RecyclerView.Adapter<GroupsOverviewViewHolder>() {
    private var groups = listOf<Group>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsOverviewViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = GroupOverviewCardBinding.inflate(view, parent, false)

        return GroupsOverviewViewHolder(binding).apply {
            itemView.setOnClickListener {
                onGroupOverviewClicked(groups[adapterPosition].groupId)
            }
        }
    }

    override fun onBindViewHolder(holder: GroupsOverviewViewHolder, position: Int) {
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

class GroupsOverviewViewHolder(val binding: GroupOverviewCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(group: Group) {
        binding.groupNameTextView.text = group.groupName
        binding.dateTextView.text = formatDate(group.creationDate)
        binding.totalExpenseTextView.text = "₹" + group.totalExpense.roundOff()
    }

}