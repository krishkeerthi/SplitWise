package com.example.splitwise.ui.fragment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.DropdownBinding
import com.example.splitwise.databinding.IconBottomSheetItemBinding

class PayerArrayAdapter(
    context: Context, layout: Int, private val payers: List<Member>
) : ArrayAdapter<Member>(context, layout, payers) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: IconBottomSheetItemBinding

        var convertView = convertView
        if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)
            binding = IconBottomSheetItemBinding.inflate(inflater, parent, false)

            convertView = binding.root
            binding.itemTextView.text = payers[position].name
            binding.itemImageView.setImageResource(R.drawable.ic_baseline_person_24)
        }

        return convertView

    }
}