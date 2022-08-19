package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.databinding.ChooseGroupCardBinding

class ChooseGroupAdapter(val onItemChecked: (Group, Boolean) -> Unit) :
    RecyclerView.Adapter<ChooseGroupViewHolder>() {
    private var groups = listOf<Group>()
    private var selectedAllGroups: Boolean = false
    private var selectedGroupIds = listOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseGroupViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = ChooseGroupCardBinding.inflate(view, parent, false)

        return ChooseGroupViewHolder(binding).apply {
            binding.selectedCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked)
                    onItemChecked(groups[adapterPosition], true)
                else
                    onItemChecked(groups[adapterPosition], false)
            }
        }
    }

    override fun onBindViewHolder(holder: ChooseGroupViewHolder, position: Int) {
        val group = groups[position]

        if (!selectedAllGroups)
            holder.bind(group, selectedGroupIds)
        else
            holder.bindAndCheck(group)
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    fun updateGroups(groups: List<Group>, selectedGroupIds: List<Int>) {
        this.groups = groups
        this.selectedGroupIds = selectedGroupIds
        notifyDataSetChanged()
    }

    fun selectAllGroups() {
        selectedAllGroups = true
        notifyDataSetChanged()
    }
}

class ChooseGroupViewHolder(val binding: ChooseGroupCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(group: Group, selectedGroupIds: List<Int>) {
        binding.groupNameTextView.text = group.groupName

        if(group.groupIcon != null) {
            binding.groupImageView.setImageURI(group.groupIcon)
            binding.groupImageHolder.visibility= View.INVISIBLE
            binding.groupImageHolderImage.visibility = View.INVISIBLE
            binding.groupImageView.visibility = View.VISIBLE
        }
        else{
            binding.groupImageHolder.visibility= View.VISIBLE
            binding.groupImageHolderImage.visibility = View.VISIBLE
            binding.groupImageView.visibility = View.INVISIBLE
        }

        if(group.groupId in selectedGroupIds)
            binding.selectedCheckBox.isChecked = true
    }

    fun bindAndCheck(group: Group) {
        binding.groupNameTextView.text = group.groupName
        binding.selectedCheckBox.isChecked = true
    }
}