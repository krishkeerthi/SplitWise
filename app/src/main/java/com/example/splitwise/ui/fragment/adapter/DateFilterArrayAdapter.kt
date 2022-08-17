package com.example.splitwise.ui.fragment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.splitwise.databinding.IconBottomSheetItemBinding
import com.example.splitwise.util.*

class DateFilterArrayAdapter(
    context: Context, layout: Int, private val dateFilters: List<DateFilter>
) : ArrayAdapter<DateFilter>(context, layout, dateFilters) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: IconBottomSheetItemBinding

        var convertView = convertView

        if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            binding = IconBottomSheetItemBinding.inflate(inflater, parent, false)

            convertView = binding.root

            binding.itemTextView.text = dateFilters[position].name.lowercase().titleCase()
            binding.itemImageView.setImageResource(getDateFilterDrawableResource(dateFilters[position].ordinal))
        }

        return convertView
    }
}