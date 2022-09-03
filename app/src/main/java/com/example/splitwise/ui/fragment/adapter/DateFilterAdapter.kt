package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.databinding.IconBottomSheetItemBinding
import com.example.splitwise.util.DateFilter
import com.example.splitwise.util.getDateFilterDrawableResource
import com.example.splitwise.util.titleCase

class DateFilterAdapter(
    private val dateFilters: List<DateFilter>,
    val onItemClicked: (DateFilter) -> Unit
) : RecyclerView.Adapter<DateFilterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateFilterViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = IconBottomSheetItemBinding.inflate(view, parent, false)

        return DateFilterViewHolder(binding).apply {
            itemView.setOnClickListener {
                onItemClicked(dateFilters[absoluteAdapterPosition])
            }
        }
    }

    override fun onBindViewHolder(holder: DateFilterViewHolder, position: Int) {
        holder.bind(dateFilters[position])
    }

    override fun getItemCount(): Int {
        return dateFilters.size
    }
}

class DateFilterViewHolder(val binding: IconBottomSheetItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(dateFilter: DateFilter) {
        binding.itemTextView.text = dateFilter.name.lowercase().titleCase()
        binding.itemImageView.setImageResource(getDateFilterDrawableResource(dateFilter.ordinal))
    }

}