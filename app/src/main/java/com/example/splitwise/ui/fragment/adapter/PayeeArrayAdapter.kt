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
)
    : ArrayAdapter<Member>(context, layout, payees) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: DropdownBinding

        return if(convertView != null){
            val inflater = LayoutInflater.from(parent.context)
            binding = DropdownBinding.inflate(inflater, parent, false)

            binding.dropdownTextView.text = payees[position].name

            binding.root
        }
        else
            super.getView(position, convertView, parent)

    }
}