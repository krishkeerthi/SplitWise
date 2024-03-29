package com.example.splitwise.ui.fragment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.DropdownBinding

class PayeeArrayAdapter(
    context: Context, layout: Int, private val payees: List<Member>
) : ArrayAdapter<Member>(context, layout, payees) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: DropdownBinding

        var convertView = convertView

        if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            binding = DropdownBinding.inflate(inflater, parent, false)

            convertView = binding.root
            binding.dropdownTextView.text = payees[position].name
        }

        return convertView

    }
}