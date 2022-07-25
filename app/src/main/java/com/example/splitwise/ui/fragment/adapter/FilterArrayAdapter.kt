package com.example.splitwise.ui.fragment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.splitwise.databinding.DropdownBinding

class FilterArrayAdapter<T: Enum<T>>(
    context: Context, layout: Int, private val list: List<T>
)
    : ArrayAdapter<T>(context, layout, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: DropdownBinding

        return if(convertView != null){
            val inflater = LayoutInflater.from(parent.context)
            binding = DropdownBinding.inflate(inflater, parent, false)

            binding.dropdownTextView.text = list[position].name

            binding.root
        }
        else
            super.getView(position, convertView, parent)

    }
}