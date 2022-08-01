package com.example.splitwise.ui.fragment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.splitwise.databinding.DropdownBinding
import com.example.splitwise.util.Category
import com.example.splitwise.util.titleCase

class CategoryArrayAdapter(
    context: Context, layout: Int, private val categories: List<Category>
) : ArrayAdapter<Category>(context, layout, categories) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: DropdownBinding

        var convertView = convertView

        if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            binding = DropdownBinding.inflate(inflater, parent, false)

            convertView = binding.root
            binding.dropdownTextView.text = categories[position].name.lowercase().titleCase()
        }

        return convertView

    }
}