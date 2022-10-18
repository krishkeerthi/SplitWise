package com.example.splitwise.ui.fragment.adapter

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.databinding.EmptyCardBinding
import com.example.splitwise.databinding.GroupCard1Binding
import com.example.splitwise.databinding.GroupCard2Binding
import com.example.splitwise.model.ExpenseMember
import com.example.splitwise.model.MemberPaymentStatsDetail
import com.example.splitwise.util.*


class GroupsAdapter(
    val onGroupClicked: (Int, View) -> Unit,
    val onImageClicked: (Int, String?, String, View) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var groups = listOf<Group>()

    private val GROUPVIEW = 1
    private val EMPTYVIEW = 2

    @SuppressLint("RestrictedApi")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
        val viewHolder: RecyclerView.ViewHolder

        viewHolder = when (viewType) {
            GROUPVIEW -> {
                val binding = GroupCard2Binding.inflate(view, parent, false)
                GroupsViewHolder(binding).apply {

                    binding.groupIconCard.setOnClickListener {
                        itemView.ripple(itemView.context)
                        onImageClicked(
                            groups[absoluteAdapterPosition].groupId,
                            groups[absoluteAdapterPosition].groupIcon?.toString(),
                            groups[absoluteAdapterPosition].groupName,

                            if (groups[absoluteAdapterPosition].groupIcon != null) binding.groupImageView
                            else binding.groupImageHolderImage
                        )
                    }

                    itemView.setOnClickListener {
                        it.ripple(it.context)
                        onGroupClicked(groups[absoluteAdapterPosition].groupId, itemView)
                    }

                }
            }
            EMPTYVIEW -> {
                val binding = EmptyCardBinding.inflate(view, parent, false)
                GroupEmptyViewHolder(binding)
            }
            else -> {
                val binding = GroupCard2Binding.inflate(view, parent, false)
                GroupsViewHolder(binding).apply {

                    binding.groupIconCard.setOnClickListener {
                        itemView.ripple(itemView.context)
                        onImageClicked(
                            groups[absoluteAdapterPosition].groupId,
                            groups[absoluteAdapterPosition].groupIcon?.toString(),
                            groups[absoluteAdapterPosition].groupName,

                            if (groups[absoluteAdapterPosition].groupIcon != null) binding.groupImageView
                            else binding.groupImageHolderImage
                        )
                    }

                    itemView.setOnClickListener {
                        it.ripple(it.context)
                        onGroupClicked(groups[absoluteAdapterPosition].groupId, itemView)
                    }

                }
            }
        }

        return viewHolder
//        val binding = GroupCard2Binding.inflate(view, parent, false)
//
//        return GroupsViewHolder(binding).apply {
//
//            binding.groupIconCard.setOnClickListener {
//                itemView.ripple(itemView.context)
//                onImageClicked(
//                    groups[absoluteAdapterPosition].groupId,
//                    groups[absoluteAdapterPosition].groupIcon?.toString(),
//                    groups[absoluteAdapterPosition].groupName,
//
//                    if (groups[absoluteAdapterPosition].groupIcon != null) binding.groupImageView
//                    else binding.groupImageHolderImage
//                )
//            }
//
//            itemView.setOnClickListener {
//                it.ripple(it.context)
//                onGroupClicked(groups[absoluteAdapterPosition].groupId, itemView)
//            }
//
//        }
        // setting transition name
//            binding.groupImageView.setOnClickListener {
//                it.ripple(it.context)
//                onImageClicked(
//                    groups[absoluteAdapterPosition].groupId,
//                    groups[absoluteAdapterPosition].groupIcon?.toString(),
//                    groups[absoluteAdapterPosition].groupName,
//
//                    if (groups[absoluteAdapterPosition].groupIcon != null) binding.groupImageView
//                    else binding.groupImageHolderImage
//                )
//            }
//            binding.groupImageHolder.setOnClickListener {
//                it.ripple(it.context)
//                onImageClicked(
//                    groups[absoluteAdapterPosition].groupId,
//                    groups[absoluteAdapterPosition].groupIcon?.toString(),
//                    groups[absoluteAdapterPosition].groupName,
//
//                    if (groups[absoluteAdapterPosition].groupIcon != null) binding.groupImageView
//                    else binding.groupImageHolderImage
//                )
//            }
//            binding.textLayout.setOnClickListener {
//                it.ripple(it.context)
//                onGroupClicked(groups[absoluteAdapterPosition].groupId, itemView)
//            }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            GROUPVIEW -> {
                val holder = holder as GroupsViewHolder
                val group = groups[position]

                holder.bind(group)
            }
            EMPTYVIEW -> {
                val holder = holder as GroupEmptyViewHolder
                holder.bind()
            }
        }
    }
//    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
//        val group = groups[position]
//
//        holder.bind(group)
//    }

    override fun getItemCount(): Int {
        return groups.size
    }

    fun updateGroups(groups: List<Group>) {
        val diffCallback = GroupsDiffCallback(this.groups, groups)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        val emptyGroupAddedGroups = addEmptyGroup(groups)
        this.groups = emptyGroupAddedGroups
       // notifyDataSetChanged()

        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int {
        return if (groups[position].groupId != -2)
            GROUPVIEW
        else
            EMPTYVIEW
    }

    private fun addEmptyGroup(groups: List<Group>): List<Group> {
        val mutableGroups = groups.toMutableList()
        mutableGroups.add(dummyGroup)
        return mutableGroups.toList()
    }
}

class GroupsViewHolder(val binding: GroupCard2Binding) : RecyclerView.ViewHolder(binding.root) {

    private val resources = binding.root.resources

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

            binding.groupImageView.setImageBitmap(null)
            //Handler(Looper.getMainLooper()).postDelayed({
            binding.groupImageView.setImageBitmap(
                getRoundedCroppedBitmap(
                    decodeSampledBitmapFromUri(
                        binding.root.context,
                        group.groupIcon,
                        48.dpToPx(resources.displayMetrics),
                        48.dpToPx(resources.displayMetrics)
                    )!!
                )
            )
            //}, resources.getInteger(R.integer.reply_motion_duration_large).toLong())

//            binding.groupImageView.setImageBitmap(decodeSampledBitmapFromUri(
//                binding.root.context, group.groupIcon, 48.dpToPx(resources.displayMetrics), 48.dpToPx(resources.displayMetrics)
//            ))
            ///binding.groupImageView.setImageURI(group.groupIcon)

            binding.groupImageHolder.visibility = View.INVISIBLE
            binding.groupImageHolderImage.visibility = View.INVISIBLE
            binding.groupImageView.visibility = View.VISIBLE
        } else {
            //binding.groupImageView.setImageResource(R.drawable.ic_baseline_people_24)
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

class GroupEmptyViewHolder(val binding: EmptyCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind() {

    }
}

class GroupsDiffCallback(private val oldItems: List<Group>, private val newItems: List<Group>)
    : DiffUtil.Callback(){
    override fun getOldListSize() = oldItems.size

    override fun getNewListSize() = newItems.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition].groupId == newItems[newItemPosition].groupId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }
}