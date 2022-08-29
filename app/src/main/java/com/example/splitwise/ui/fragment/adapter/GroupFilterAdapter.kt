package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.databinding.IconBottomSheetItemBinding
import com.example.splitwise.databinding.IconSelectedBottomSheetItemBinding
import com.example.splitwise.util.GroupFilter
import com.example.splitwise.util.getGroupFilterDrawableResource
import com.example.splitwise.util.titleCase

class GroupFilterAdapter(
    private val filters: List<GroupFilter>,
    private val remainingFilters: List<GroupFilter>,
    val onItemClicked: (GroupFilter) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val SELECTED = 1
    private val UNSELECTED = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder
        val view = LayoutInflater.from(parent.context)

        viewHolder = when (viewType) {
            SELECTED -> {
                val binding = IconSelectedBottomSheetItemBinding.inflate(view, parent, false)
                GroupFilterSelectedViewHolder(binding)
            }
            UNSELECTED -> {
                val binding = IconBottomSheetItemBinding.inflate(view, parent, false)
                GroupFilterViewHolder(binding).apply {
                    itemView.setOnClickListener {
                        onItemClicked(filters[absoluteAdapterPosition])
                    }
                }
            }
            else -> {
                val binding = IconBottomSheetItemBinding.inflate(view, parent, false)
                GroupFilterViewHolder(binding).apply {
                    itemView.setOnClickListener {
                        onItemClicked(filters[absoluteAdapterPosition])
                    }
                }
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            SELECTED -> {
                val viewHolder = holder as GroupFilterSelectedViewHolder
                viewHolder.bind(filters[position])
            }
            UNSELECTED -> {
                val viewHolder = holder as GroupFilterViewHolder
                viewHolder.bind(filters[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return filters.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (filters[position] in remainingFilters)
            UNSELECTED
        else
            SELECTED
    }

//    fun updateFilters() {
//        notifyDataSetChanged()
//    }
}

class GroupFilterViewHolder(val binding: IconBottomSheetItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(filter: GroupFilter) {
        binding.itemTextView.text = filter.name.lowercase().titleCase()
        binding.itemImageView.setImageResource(getGroupFilterDrawableResource(filter.ordinal))
    }
}

class GroupFilterSelectedViewHolder(val binding: IconSelectedBottomSheetItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(filter: GroupFilter) {
        binding.itemTextView.text = filter.name.lowercase().titleCase()
    }
}