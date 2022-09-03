package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.databinding.IconBottomSheetItemBinding
import com.example.splitwise.util.AmountFilter
import com.example.splitwise.util.getDateFilterDrawableResource
import com.example.splitwise.util.titleCase

class AmountFilterAdapter(
    private val amountFilters: List<AmountFilter>,
    val onItemClicked: (AmountFilter) -> Unit
) : RecyclerView.Adapter<AmountFilterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmountFilterViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = IconBottomSheetItemBinding.inflate(view, parent, false)

        return AmountFilterViewHolder(binding).apply {
            itemView.setOnClickListener {
                onItemClicked(amountFilters[absoluteAdapterPosition])
            }
        }
    }

    override fun onBindViewHolder(holder: AmountFilterViewHolder, position: Int) {
        holder.bind(amountFilters[position])
    }

    override fun getItemCount(): Int {
        return amountFilters.size
    }
}

class AmountFilterViewHolder(val binding: IconBottomSheetItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(amountFilter: AmountFilter) {
        binding.itemTextView.text = amountFilter.name.lowercase().titleCase()
        binding.itemImageView.setImageResource(getDateFilterDrawableResource(amountFilter.ordinal))
    }

}