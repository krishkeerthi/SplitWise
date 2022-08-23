package com.example.splitwise.ui.fragment.adapter

import android.content.ContentValues
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.databinding.GroupCard1Binding
import com.example.splitwise.databinding.GroupCardBinding
import com.example.splitwise.util.downloadBitmap
import com.example.splitwise.util.formatDate
import com.example.splitwise.util.roundOff
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupsAdapter(
    val onExpenseClicked: (Int) -> Unit,
    val onImageClicked: (Int, String?, String) -> Unit
) : RecyclerView.Adapter<GroupsViewHolder>() {
    private var groups = listOf<Group>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = GroupCard1Binding.inflate(view, parent, false)

        return GroupsViewHolder(binding).apply {
            binding.groupImageView.setOnClickListener {
                onImageClicked(groups[absoluteAdapterPosition].groupId,
                    groups[absoluteAdapterPosition].groupIcon?.toString(),
                    groups[absoluteAdapterPosition].groupName
                )
            }
            binding.groupImageHolder.setOnClickListener {
                onImageClicked(groups[absoluteAdapterPosition].groupId,
                    groups[absoluteAdapterPosition].groupIcon?.toString(),
                    groups[absoluteAdapterPosition].groupName
                )
            }
            binding.textLayout.setOnClickListener{
                onExpenseClicked(groups[absoluteAdapterPosition].groupId)
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

    val resources = binding.root.resources

    fun bind(group: Group) {
        binding.groupNameTextView.text = group.groupName
        binding.groupExpenseTextView.text = "₹" + group.totalExpense.roundOff()
        binding.groupCreationDateTextView.text = getDateStringResource(formatDate(group.creationDate))

        if(group.groupIcon != null) {
            binding.groupImageView.setImageURI(group.groupIcon)
            binding.groupImageHolder.visibility= View.INVISIBLE
            binding.groupImageHolderImage.visibility = View.INVISIBLE
            binding.groupImageView.visibility = View.VISIBLE
        }
        else {
            binding.groupImageView.setImageResource(R.drawable.ic_baseline_people_24)
            binding.groupImageHolder.visibility= View.VISIBLE
            binding.groupImageHolderImage.visibility = View.VISIBLE
            binding.groupImageView.visibility = View.INVISIBLE
        }
    }

    private fun getDateStringResource(formatDate: String): String {
        return when(formatDate){
            //"Today" -> resources.getString(R.string.today)
            "Yesterday" -> resources.getString(R.string.yesterday)
            else -> formatDate
        }
    }

}