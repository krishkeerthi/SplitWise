package com.example.splitwise.ui.fragment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.splitwise.databinding.DropdownBinding
import com.example.splitwise.util.titleCase

class FilterArrayAdapter<T : Enum<T>>(
    context: Context, layout: Int, private val list: List<T>
) : ArrayAdapter<T>(context, layout, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: DropdownBinding

        var convertView = convertView

        if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            binding = DropdownBinding.inflate(inflater, parent, false)

            convertView = binding.root
            binding.dropdownTextView.text = list[position].name.lowercase().titleCase()
        }

        return convertView

    }
}