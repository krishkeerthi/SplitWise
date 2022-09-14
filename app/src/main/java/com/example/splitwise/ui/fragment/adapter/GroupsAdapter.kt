package com.example.splitwise.ui.fragment.adapter

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.databinding.GroupCard1Binding
import com.example.splitwise.util.formatDate
import com.example.splitwise.util.ripple
import com.example.splitwise.util.roundOff


class GroupsAdapter(
    val onGroupClicked: (Int, View) -> Unit,
    val onImageClicked: (Int, String?, String, View) -> Unit
) : RecyclerView.Adapter<GroupsViewHolder>() {
    private var groups = listOf<Group>()

    @SuppressLint("RestrictedApi")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = GroupCard1Binding.inflate(view, parent, false)

        return GroupsViewHolder(binding).apply {

            // setting transition name
            binding.groupImageView.setOnClickListener {
                it.ripple(it.context)
                onImageClicked(
                    groups[absoluteAdapterPosition].groupId,
                    groups[absoluteAdapterPosition].groupIcon?.toString(),
                    groups[absoluteAdapterPosition].groupName,

                    if (groups[absoluteAdapterPosition].groupIcon != null) binding.groupImageView
                    else binding.groupImageHolderImage
                )
            }
            binding.groupImageHolder.setOnClickListener {
                it.ripple(it.context)
                onImageClicked(
                    groups[absoluteAdapterPosition].groupId,
                    groups[absoluteAdapterPosition].groupIcon?.toString(),
                    groups[absoluteAdapterPosition].groupName,

                    if (groups[absoluteAdapterPosition].groupIcon != null) binding.groupImageView
                    else binding.groupImageHolderImage
                )
            }
            binding.textLayout.setOnClickListener {
                it.ripple(it.context)
                onGroupClicked(groups[absoluteAdapterPosition].groupId, itemView)
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
        ViewCompat.setTransitionName(
            binding.root,
            String.format(resources.getString(R.string.group_card_transition_name), group.groupId)
        )
        ViewCompat.setTransitionName(
            binding.groupImageView,
            String.format(resources.getString(R.string.group_image_transition_name), group.groupId)
        )
        ViewCompat.setTransitionName(
            binding.groupImageHolderImage,
            String.format(
                resources.getString(R.string.group_empty_image_transition_name),
                group.groupId
            )
        )

        binding.groupNameTextView.text = group.groupName
        binding.groupExpenseTextView.text = "₹" + group.totalExpense.roundOff()
        binding.groupCreationDateTextView.text =
            getDateStringResource(formatDate(group.creationDate))

        if (group.groupIcon != null) {
            Log.d(TAG, "bind: group icon${group.groupIcon}")
            binding.groupImageView.setImageURI(group.groupIcon)
            binding.groupImageHolder.visibility = View.INVISIBLE
            binding.groupImageHolderImage.visibility = View.INVISIBLE
            binding.groupImageView.visibility = View.VISIBLE
        } else {
            binding.groupImageView.setImageResource(R.drawable.ic_baseline_people_24)
            binding.groupImageHolder.visibility = View.VISIBLE
            binding.groupImageHolderImage.visibility = View.VISIBLE
            binding.groupImageView.visibility = View.INVISIBLE
        }
    }

    private fun getDateStringResource(formatDate: String): String {
        return when (formatDate) {
            //"Today" -> resources.getString(R.string.today)
            "Yesterday" -> resources.getString(R.string.yesterday)
            else -> formatDate
        }
    }

}